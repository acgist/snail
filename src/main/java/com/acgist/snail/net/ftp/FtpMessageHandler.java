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
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.ThreadUtils;

/**
 * FTP消息
 * 
 * @author acgist
 * @since 1.0.0
 */
public class FtpMessageHandler extends TcpMessageHandler implements IMessageCodec<String> {

	private static final Logger LOGGER = LoggerFactory.getLogger(FtpMessageHandler.class);
	
	/**
	 * 命令超时时间
	 */
	private static final Duration TIMEOUT = Duration.ofSeconds(SystemConfig.SEND_TIMEOUT);
	/**
	 * 每条消息分隔符
	 */
	private static final String SPLIT = "\r\n";
	/**
	 * 命令结束正则表达式
	 */
	private static final String END_REGEX = "\\d{3} .*";
	
	/**
	 * Socket
	 */
	private Socket inputSocket;
	/**
	 * 输入流
	 */
	private InputStream inputStream;
	/**
	 * 断点续传
	 */
	private boolean range = false;
	/**
	 * 错误信息
	 */
	private String failMessage;
	/**
	 * 编码：默认GBK
	 */
	private String charset = SystemConfig.CHARSET_GBK;
	/**
	 * 命令锁：等待命令执行响应
	 */
	private final AtomicBoolean commandLock = new AtomicBoolean(false);
	
	public FtpMessageHandler() {
		final var multilineMessageCodec = new MultilineMessageCodec(this, SPLIT, END_REGEX);
		final var lineMessageCodec = new LineMessageCodec(multilineMessageCodec, SPLIT);
		final var stringMessageCodec = new StringMessageCodec(lineMessageCodec);
		this.messageCodec = stringMessageCodec;
	}

	@Override
	public void onMessage(String message) throws NetException {
		LOGGER.debug("收到FTP响应：{}", message);
		if(StringUtils.startsWith(message, "530 ")) { // 登陆失败
			this.failMessage = "服务器需要登陆授权";
			this.close();
			this.unlockCommand();
		} else if(StringUtils.startsWith(message, "550 ")) { // 文件不存在
			this.failMessage = "文件不存在";
			this.close();
			this.unlockCommand();
		} else if(StringUtils.startsWith(message, "421 ")) { // Socket打开失败
			this.failMessage = "打开连接失败";
			this.close();
			this.unlockCommand();
		} else if(StringUtils.startsWith(message, "350 ")) { // 断点续传
			this.range = true;
		} else if(StringUtils.startsWith(message, "220 ")) { // 退出系统
		} else if(StringUtils.startsWith(message, "230 ")) { // 登陆成功
			this.unlockCommand();
		} else if(StringUtils.startsWith(message, "226 ")) { // 下载完成
		} else if(StringUtils.startsWith(message, "502 ")) { // 不支持命令：FEAT
			this.unlockCommand();
		} else if(StringUtils.startsWith(message, "211-")) { // 服务器状态：扩展命令编码查询
			// 判断是否支持UTF8
			if(message.toUpperCase().contains(SystemConfig.CHARSET_UTF8)) {
				this.charset = SystemConfig.CHARSET_UTF8;
				LOGGER.debug("FTP设置编码：{}", this.charset);
			}
			this.unlockCommand();
		} else if(StringUtils.startsWith(message, "227 ")) { // 进入被动模式：获取下载Socket的IP和端口
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
					this.inputSocket = new Socket(host, port);
				} catch (IOException e) {
					LOGGER.error("打开FTP远程Socket异常", e);
				}
			}
		} else if(StringUtils.startsWith(message, "150 ")) { // 打开数据连接
			if(this.inputSocket == null) {
				throw new NetException("请切换到被动模式");
			}
			try {
				this.inputStream = this.inputSocket.getInputStream();
				this.unlockCommand();
			} catch (IOException e) {
				LOGGER.error("打开FTP远程输入流异常", e);
			}
		}
	}
	
	/**
	 * 是否支持断点续传
	 */
	public boolean range() {
		return this.range;
	}
	
	/**
	 * 登录锁
	 */
	public void loginLock() {
		this.lockCommand();
	}
	
	/**
	 * 字符编码
	 */
	public String charset() {
		return this.charset;
	}
	
	/**
	 * 字符编码（加锁）
	 */
	public String charsetLock() {
		this.lockCommand();
		return this.charset;
	}
	
	/**
	 * 获取输入流，阻塞线程
	 */
	public InputStream inputStream() {
		this.lockCommand();
		if(this.inputStream == null && this.failMessage == null) {
			this.failMessage = "下载失败";
		}
		return this.inputStream;
	}
	
	/**
	 * 获取错误信息
	 */
	public String failMessage() {
		return this.failMessage;
	}
	
	/**
	 * 重置锁
	 */
	public void resetLock() {
		this.commandLock.set(false);
	}

	@Override
	public void close() {
		this.release();
		super.close();
	}
	
	/**
	 * 释放资源：注意释放FTP下载连接资源（文件流、socket），不关闭命令通道。
	 */
	private void release() {
		IoUtils.close(this.inputStream);
		IoUtils.close(this.inputSocket);
	}
	
	/**
	 * 锁定命令
	 */
	private void lockCommand() {
		if(!this.commandLock.get()) {
			synchronized (this.commandLock) {
				if(!this.commandLock.get()) {
					ThreadUtils.wait(this.commandLock, TIMEOUT);
				}
			}
		}
	}
	
	/**
	 * 解锁命令
	 */
	private void unlockCommand() {
		synchronized (this.commandLock) {
			this.commandLock.set(true);
			this.commandLock.notifyAll();
		}
	}

}
