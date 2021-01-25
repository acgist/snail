package com.acgist.snail.net.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.net.TcpMessageHandler;
import com.acgist.snail.net.codec.IMessageDecoder;
import com.acgist.snail.net.codec.IMessageEncoder;
import com.acgist.snail.net.codec.LineMessageCodec;
import com.acgist.snail.net.codec.MultilineMessageCodec;
import com.acgist.snail.net.codec.StringMessageCodec;
import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>FTP消息代理</p>
 * 
 * @author acgist
 */
public final class FtpMessageHandler extends TcpMessageHandler implements IMessageDecoder<String> {

	private static final Logger LOGGER = LoggerFactory.getLogger(FtpMessageHandler.class);
	
	/**
	 * <p>多行消息正则表达式：{@value}</p>
	 * <p>返回多行消息FTP命令：FEAT</p>
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
	public void onMessage(String message) throws NetException {
		LOGGER.debug("处理FTP消息：{}", message);
		if(StringUtils.startsWith(message, "530 ")) {
			// 登陆失败
			this.login = false;
			this.failMessage = "登陆失败";
		} else if(StringUtils.startsWith(message, "550 ")) {
			// 文件不存在
			this.failMessage = "文件不存在";
		} else if(StringUtils.startsWith(message, "421 ")) {
			// 打开连接失败
			this.failMessage = "打开连接失败";
		} else if(StringUtils.startsWith(message, "350 ")) {
			// 支持断点续传
			this.range = true;
		} else if(StringUtils.startsWith(message, "220 ")) {
			// 退出系统
		} else if(StringUtils.startsWith(message, "230 ")) {
			// 登陆成功
			this.login = true;
		} else if(StringUtils.startsWith(message, "226 ")) {
			// 下载完成
		} else if(StringUtils.startsWith(message, "502 ")) {
			// 不支持命令
			LOGGER.debug("处理FTP消息错误（不支持命令）：{}", message);
		} else if(StringUtils.startsWith(message, "211-")) {
			// 系统状态：扩展命令FEAT
			// 判断是否支持UTF8指令
			if(message.toUpperCase().contains(SystemConfig.CHARSET_UTF8)) {
				this.charset = SystemConfig.CHARSET_UTF8;
				LOGGER.debug("设置FTP编码：{}", this.charset);
			}
		} else if(StringUtils.startsWith(message, "227 ")) {
			// 进入被动模式：打开文件下载Socket
			this.release(); // 释放旧的资源
			// 被动模式格式：227 Entering Passive Mode (127,0,0,1,36,158).
			final int opening = message.indexOf('(');
			final int closing = message.indexOf(')', opening + 1);
			if (opening >= 0 && closing > opening) {
				final String data = message.substring(opening + 1, closing);
				final StringTokenizer tokenizer = new StringTokenizer(data, ",");
				final String host = tokenizer.nextToken() + "." + tokenizer.nextToken() + "." + tokenizer.nextToken() + "." + tokenizer.nextToken();
				final int port = (Integer.parseInt(tokenizer.nextToken()) << 8) + Integer.parseInt(tokenizer.nextToken());
				try {
					this.inputSocket = new Socket();
					this.inputSocket.setSoTimeout(SystemConfig.DOWNLOAD_TIMEOUT_MILLIS);
					this.inputSocket.connect(NetUtils.buildSocketAddress(host, port), SystemConfig.CONNECT_TIMEOUT_MILLIS);
				} catch (IOException e) {
					LOGGER.error("打开文件下载Socket异常：{}-{}", host, port, e);
				}
			}
		} else if(
			StringUtils.startsWith(message, "125 ") ||
			StringUtils.startsWith(message, "150 ")
		) {
			// 打开下载文件连接
			if(this.inputSocket == null) {
				throw new NetException("请切换到被动模式");
			}
			try {
				this.inputStream = this.inputSocket.getInputStream();
			} catch (IOException e) {
				LOGGER.error("打开文件输入流异常", e);
			}
		}
		this.unlock(); // 释放命令锁
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
	 * <p>如果没有错误信息返回默认错误信息</p>
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
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>释放文件下载资源和关闭命令通道</p>
	 */
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
						LOGGER.debug("线程等待异常", e);
						Thread.currentThread().interrupt();
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

}
