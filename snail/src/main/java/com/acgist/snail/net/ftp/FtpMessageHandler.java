package com.acgist.snail.net.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.TcpMessageHandler;
import com.acgist.snail.net.codec.IMessageDecoder;
import com.acgist.snail.net.codec.IMessageEncoder;
import com.acgist.snail.net.codec.LineMessageCodec;
import com.acgist.snail.net.codec.MultilineMessageCodec;
import com.acgist.snail.net.codec.StringMessageCodec;
import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.NetUtils;

/**
 * <p>FTP消息代理</p>
 * 
 * @author acgist
 */
public final class FtpMessageHandler extends TcpMessageHandler implements IMessageDecoder<String> {

	private static final Logger LOGGER = LoggerFactory.getLogger(FtpMessageHandler.class);
	
	/**
	 * <p>多行消息正则表达式：{@value}</p>
	 */
	private static final String MULTILINE_REGEX = "\\d{3} .*";
	
	/**
	 * <p>是否登陆成功</p>
	 */
	private boolean login = false;
	/**
	 * <p>是否支持断点续传</p>
	 */
	private boolean range = false;
	/**
	 * <p>编码</p>
	 * <p>默认编码：GBK</p>
	 */
	private String charset = SystemConfig.CHARSET_GBK;
	/**
	 * <p>错误信息</p>
	 */
	private String failMessage;
	/**
	 * <p>输入流Socket</p>
	 */
	private Socket inputSocket;
	/**
	 * <p>输入流</p>
	 */
	private InputStream inputStream;
	/**
	 * <p>消息编码器</p>
	 */
	private final IMessageEncoder<String> messageEncoder;
	/**
	 * <p>命令锁</p>
	 * <p>等待命令执行响应</p>
	 */
	private final AtomicBoolean lock = new AtomicBoolean(false);
	
	public FtpMessageHandler() {
		// 注意：命令换行可能不同
		final var multilineMessageCodec = new MultilineMessageCodec(this, SymbolConfig.LINE_SEPARATOR_COMPAT, MULTILINE_REGEX);
		final var lineMessageCodec = new LineMessageCodec(multilineMessageCodec, SymbolConfig.LINE_SEPARATOR_COMPAT);
		final var stringMessageCodec = new StringMessageCodec(lineMessageCodec);
		this.messageDecoder = stringMessageCodec;
		this.messageEncoder = lineMessageCodec;
	}
	
	@Override
	public void send(String message) throws NetException {
		super.send(this.messageEncoder.encode(message), this.charset);
	}

	@Override
	public void onMessage(String message) {
		final CommandCode code = CommandCode.of(message);
		if(code == null) {
			LOGGER.debug("FTP消息没有适配：{}", message);
		} else {
			LOGGER.debug("处理FTP消息：{}", message);
			switch (code) {
				case DATA_CONNECTION_OPEN, FILE_STATUS_OKAY -> this.openInputStream();
				case SYSTEM_STATUS -> this.systemStatus(message);
				case READY_FOR_NEW_USER -> this.readyForNewUser(message);
				case FILE_ACTION_SUCCESS -> this.fileActionSuccess(message);
				case PASSIVE_MODE -> this.passiveMode(message);
				case LOGIN_SUCCESS -> this.loginSuccess();
				case FILE_ACTION_PENDING -> this.fileActionPending();
				case CONNECTION_CLOSED -> this.connectionClosed();
				case NOT_SUPPORT_COMMAND -> this.notSupportCommand(message);
				case NOT_LOGIN -> this.notLogin();
				case FILE_UNAVAILABLE -> this.fileUnavailable();
				default -> LOGGER.warn("FTP状态码没有适配：{}-{}", code, message);
			}
		}
		this.unlock();
	}
	
	/**
	 * <p>判断是否登陆成功</p>
	 * 
	 * @return 是否登陆成功
	 */
	public boolean login() {
		return this.login;
	}
	
	/**
	 * <p>判断是否支持断点续传</p>
	 * 
	 * @return 是否支持断点续传
	 */
	public boolean range() {
		return this.range;
	}
	
	/**
	 * <p>获取字符编码</p>
	 * 
	 * @return 字符编码
	 */
	public String charset() {
		return this.charset;
	}
	
	/**
	 * <p>获取错误信息</p>
	 * 
	 * @param defaultMessage 默认错误信息
	 * 
	 * @return 错误信息
	 */
	public String failMessage(String defaultMessage) {
		if(this.failMessage == null) {
			return defaultMessage;
		}
		return this.failMessage;
	}
	
	/**
	 * <p>获取文件流</p>
	 * 
	 * @return 文件流
	 * 
	 * @throws NetException 网络异常
	 */
	public InputStream inputStream() throws NetException {
		if(this.inputStream == null) {
			throw new NetException(this.failMessage("未知错误"));
		}
		return this.inputStream;
	}
	
	/**
	 * <p>释放文件下载资源</p>
	 * <p>关闭文件流、Socket，不关闭命令通道。</p>
	 */
	private void release() {
		IoUtils.close(this.inputStream);
		IoUtils.close(this.inputSocket);
	}
	
	@Override
	public void close() {
		this.release();
		super.close();
	}

	/**
	 * <p>重置命令锁</p>
	 */
	public void resetLock() {
		this.lock.set(false);
	}
	
	/**
	 * <p>添加命令锁</p>
	 */
	public void lock() {
		if(!this.lock.get()) {
			synchronized (this.lock) {
				if(!this.lock.get()) {
					try {
						this.lock.wait(SystemConfig.RECEIVE_TIMEOUT_MILLIS);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						LOGGER.debug("线程等待异常", e);
					}
				}
			}
		}
	}
	
	/**
	 * <p>释放命令锁</p>
	 */
	private void unlock() {
		synchronized (this.lock) {
			this.lock.set(true);
			this.lock.notifyAll();
		}
	}
	
	/**
	 * <p>打开输入流</p>
	 * 
	 * @see CommandCode#DATA_CONNECTION_OPEN
	 * @see CommandCode#FILE_STATUS_OKAY
	 */
	private void openInputStream() {
		if(this.inputSocket == null) {
			this.failMessage = "没有切换被动模式";
		} else {
			try {
				this.inputStream = this.inputSocket.getInputStream();
			} catch (IOException e) {
				this.failMessage = "打开输入流失败";
				LOGGER.error("打开输入流异常", e);
			}
		}
	}
	
	/**
	 * <p>系统状态或者系统帮助</p>
	 * <p>扩展命令FEAT（多行文本）</p>
	 * 
	 * @param message 消息
	 * 
	 * @see CommandCode#SYSTEM_STATUS
	 */
	private void systemStatus(String message) {
		// 判断是否支持UTF8指令
		if(message.toUpperCase().contains(SystemConfig.CHARSET_UTF8)) {
			this.charset = SystemConfig.CHARSET_UTF8;
			LOGGER.debug("设置FTP编码：{}", this.charset);
		}
	}
	
	/**
	 * <p>准备迎接新的用户</p>
	 * 
	 * @param message 消息
	 * 
	 * @see CommandCode#READY_FOR_NEW_USER
	 */
	private void readyForNewUser(String message) {
		LOGGER.debug("准备迎接新的用户：{}", message);
	}
	
	/**
	 * <p>文件操作成功</p>
	 * 
	 * @param message 消息
	 * 
	 * @see CommandCode#FILE_ACTION_SUCCESS
	 */
	private void fileActionSuccess(String message) {
		LOGGER.debug("文件操作成功：{}", message);
	}
	
	/**
	 * <p>被动模式</p>
	 * 
	 * @param message 消息
	 * 
	 * @see CommandCode#PASSIVE_MODE
	 */
	private void passiveMode(String message) {
		this.release();
		// 被动模式格式：227 Entering Passive Mode (127,0,0,1,36,158).
		final int opening = message.indexOf(SymbolConfig.Symbol.OPEN_PARENTHESIS.toChar());
		final int closing = message.indexOf(SymbolConfig.Symbol.CLOSE_PARENTHESIS.toChar(), opening + 1);
		if (opening >= 0 && closing > opening) {
			final String data = message.substring(opening + 1, closing);
			final StringTokenizer tokenizer = new StringTokenizer(data, SymbolConfig.Symbol.COMMA.toString());
			final String host = SymbolConfig.Symbol.DOT.join(tokenizer.nextToken(), tokenizer.nextToken(), tokenizer.nextToken(), tokenizer.nextToken());
			final int port = (Integer.parseInt(tokenizer.nextToken()) << 8) + Integer.parseInt(tokenizer.nextToken());
			try {
				this.inputSocket = new Socket();
				this.inputSocket.setSoTimeout(SystemConfig.DOWNLOAD_TIMEOUT_MILLIS);
				this.inputSocket.connect(NetUtils.buildSocketAddress(host, port), SystemConfig.CONNECT_TIMEOUT_MILLIS);
			} catch (IOException e) {
				this.failMessage = "打开输入流Socket失败";
				LOGGER.error("打开输入流Socket异常：{}-{}", host, port, e);
			}
		}
	}
	
	/**
	 * <p>登录系统</p>
	 * 
	 * @see CommandCode#LOGIN_SUCCESS
	 */
	private void loginSuccess() {
		this.login = true;
	}
	
	/**
	 * <p>等待文件操作（支持断点续传）</p>
	 * 
	 * @see CommandCode#FILE_ACTION_PENDING
	 */
	private void fileActionPending() {
		this.range = true;
	}
	
	/**
	 * <p>连接已经关闭</p>
	 * 
	 * @see CommandCode#CONNECTION_CLOSED
	 */
	private void connectionClosed() {
		this.failMessage = "打开连接失败";
	}
	
	/**
	 * <p>不支持的命令</p>
	 * 
	 * @param message 消息
	 * 
	 * @see CommandCode#NOT_SUPPORT_COMMAND
	 */
	private void notSupportCommand(String message) {
		LOGGER.debug("不支持的命令：{}", message);
	}
	
	/**
	 * <p>没有登录</p>
	 * 
	 * @see CommandCode#NOT_LOGIN
	 */
	private void notLogin() {
		this.login = false;
		this.failMessage = "没有登录";
	}
	
	/**
	 * <p>文件无效</p>
	 * 
	 * @see CommandCode#FILE_UNAVAILABLE
	 */
	private void fileUnavailable() {
		this.failMessage = "文件无效";
	}

}
