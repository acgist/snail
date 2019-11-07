package com.acgist.snail.net.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.time.Duration;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.TcpMessageHandler;
import com.acgist.snail.net.codec.IMessageCodec;
import com.acgist.snail.net.codec.impl.LineMessageCodec;
import com.acgist.snail.net.codec.impl.MultilineMessageCodec;
import com.acgist.snail.net.codec.impl.StringMessageCodec;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.ThreadUtils;

/**
 * FTP消息代理
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class FtpMessageHandler extends TcpMessageHandler implements IMessageCodec<String> {

	private static final Logger LOGGER = LoggerFactory.getLogger(FtpMessageHandler.class);
	
	/**
	 * 命令超时时间
	 */
	private static final Duration TIMEOUT = Duration.ofSeconds(SystemConfig.RECEIVE_TIMEOUT);
	/**
	 * 消息分隔符
	 */
	private static final String SPLIT = "\r\n";
	/**
	 * 消息结束正则表达式
	 */
	private static final String END_REGEX = "\\d{3} .*";
	
	/**
	 * 输入流Socket
	 */
	private Socket inputSocket;
	/**
	 * 输入流
	 */
	private InputStream inputStream;
	/**
	 * 登陆状态
	 */
	private boolean login = false;
	/**
	 * 断点续传
	 */
	private boolean range = false;
	/**
	 * 默认编码：GBK
	 */
	private String charset = SystemConfig.CHARSET_GBK;
	/**
	 * 错误信息
	 */
	private String failMessage;
	/**
	 * 命令锁：等待命令执行响应
	 */
	private final AtomicBoolean lock = new AtomicBoolean(false);
	
	public FtpMessageHandler() {
		final var multilineMessageCodec = new MultilineMessageCodec(this, SPLIT, END_REGEX);
		final var lineMessageCodec = new LineMessageCodec(multilineMessageCodec, SPLIT);
		final var stringMessageCodec = new StringMessageCodec(lineMessageCodec);
		this.messageCodec = stringMessageCodec;
	}

	@Override
	public void onMessage(String message) throws NetException {
		LOGGER.debug("处理FTP消息：{}", message);
		if(StringUtils.startsWith(message, "530 ")) { // 登陆失败
			this.login = false;
			this.failMessage = "登陆失败";
			this.close();
		} else if(StringUtils.startsWith(message, "550 ")) { // 文件不存在
			this.failMessage = "文件不存在";
			this.close();
		} else if(StringUtils.startsWith(message, "421 ")) { // 打开连接失败：Socket
			this.failMessage = "打开连接失败";
			this.close();
		} else if(StringUtils.startsWith(message, "350 ")) { // 断点续传
			this.range = true;
		} else if(StringUtils.startsWith(message, "220 ")) { // 退出系统
		} else if(StringUtils.startsWith(message, "230 ")) { // 登陆成功
			this.login = true;
		} else if(StringUtils.startsWith(message, "226 ")) { // 下载完成
		} else if(StringUtils.startsWith(message, "502 ")) { // 不支持命令：FEAT
		} else if(StringUtils.startsWith(message, "211-")) { // 服务器状态：扩展命令编码查询
			// 判断是否支持UTF8
			if(message.toUpperCase().contains(SystemConfig.CHARSET_UTF8)) {
				this.charset = SystemConfig.CHARSET_UTF8;
				LOGGER.debug("设置FTP编码：{}", this.charset);
			}
		} else if(StringUtils.startsWith(message, "227 ")) { // 进入被动模式：获取文件下载Socket的IP和端口
			release(); // 释放旧的资源
			final int opening = message.indexOf('(');
			final int closing = message.indexOf(')', opening + 1);
			if (closing > 0) {
				final String data = message.substring(opening + 1, closing);
				final StringTokenizer tokenizer = new StringTokenizer(data, ",");
				final String host = tokenizer.nextToken() + "." + tokenizer.nextToken() + "." + tokenizer.nextToken() + "." + tokenizer.nextToken();
//				final int port = Integer.parseInt(tokenizer.nextToken()) * 256 + Integer.parseInt(tokenizer.nextToken());
				final int port = (Integer.parseInt(tokenizer.nextToken()) << 8) + Integer.parseInt(tokenizer.nextToken());
				try {
					this.inputSocket = new Socket();
					this.inputSocket.setSoTimeout(SystemConfig.DOWNLOAD_TIMEOUT_MILLIS);
					this.inputSocket.connect(NetUtils.buildSocketAddress(host, port), SystemConfig.CONNECT_TIMEOUT_MILLIS);
				} catch (IOException e) {
					LOGGER.error("打开FTP远程Socket异常", e);
				}
			}
		} else if(StringUtils.startsWith(message, "150 ")) { // 打开下载文件连接
			if(this.inputSocket == null) {
				throw new NetException("请切换到被动模式");
			}
			try {
				this.inputStream = this.inputSocket.getInputStream();
			} catch (IOException e) {
				LOGGER.error("打开FTP远程输入流异常", e);
			}
		}
		this.unlock();
	}
	
	/**
	 * @return 是否登陆成功
	 */
	public boolean login() {
		return this.login;
	}
	
	/**
	 * @return 是否支持断点续传
	 */
	public boolean range() {
		return this.range;
	}
	
	/**
	 * @return 字符编码
	 */
	public String charset() {
		return this.charset;
	}
	
	/**
	 * @return 错误信息
	 */
	public String failMessage(String defaultMessage) {
		if(this.failMessage == null) {
			return defaultMessage;
		}
		return this.failMessage;
	}
	
	/**
	 * @return 文件流
	 */
	public InputStream inputStream() throws NetException {
		if(this.inputStream == null) {
			throw new NetException(this.failMessage("未知错误"));
		}
		return this.inputStream;
	}
	
	@Override
	public void close() {
		this.release();
		super.close();
	}
	
	/**
	 * <p>释放资源</p>
	 * <p>释放FTP下载连接资源（文件流、Socket），不关闭命令通道。</p>
	 */
	private void release() {
		IoUtils.close(this.inputStream);
		IoUtils.close(this.inputSocket);
	}

	/**
	 * 重置锁
	 */
	public void resetLock() {
		this.lock.set(false);
	}
	
	/**
	 * 锁定命令
	 */
	public void lock() {
		if(!this.lock.get()) {
			synchronized (this.lock) {
				if(!this.lock.get()) {
					ThreadUtils.wait(this.lock, TIMEOUT);
				}
			}
		}
	}
	
	/**
	 * 解锁命令
	 */
	private void unlock() {
		synchronized (this.lock) {
			this.lock.set(true);
			this.lock.notifyAll();
		}
	}

}
