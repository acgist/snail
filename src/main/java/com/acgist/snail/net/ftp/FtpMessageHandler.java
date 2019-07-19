package com.acgist.snail.net.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.TcpMessageHandler;
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
public class FtpMessageHandler extends TcpMessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(FtpMessageHandler.class);
	
	public static final String SPLIT = "\r\n"; // 处理粘包分隔符
	
	private boolean range = false; // 断点续传
	
	private String failMessage; // 错误信息
	
	private Socket socket; // Socket
	private InputStream inputStream; // 输入流
	
	private String charset = SystemConfig.CHARSET_GBK; // 编码：默认GBK
	
	private final AtomicBoolean commandLock = new AtomicBoolean(false); // 命令等待锁
	
	/**
	 * 支持UTF8命令
	 */
	private static final String COMMAND_UTF8 = "UTF8";
	/**
	 * 命令结束正则表达式
	 */
	private static final String COMMAND_END_REGEX = "\\d{3} .*";
	
	public FtpMessageHandler() {
		super(SPLIT);
	}

	@Override
	public void onMessage(ByteBuffer attachment) throws NetException {
		String tmp;
		StringBuffer command = new StringBuffer();
		String content = IoUtils.readContent(attachment, charset);
		if(content.contains(SPLIT)) {
			int index = content.indexOf(SPLIT);
			while(index >= 0) {
				tmp = content.substring(0, index);
				if(tmp.matches(COMMAND_END_REGEX)) {
					command.append(tmp);
					oneMessage(command.toString());
					command.setLength(0);
				} else {
					command.append(tmp).append(SPLIT);
				}
				content = content.substring(index + SPLIT.length());
				index = content.indexOf(SPLIT);
			}
		}
	}
	
	/**
	 * 处理单条消息
	 */
	private void oneMessage(String message) throws NetException {
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
		} else if(StringUtils.startsWith(message, "350 ")) { // 端点续传
			this.range = true;
		} else if(StringUtils.startsWith(message, "220 ")) { // 退出系统
		} else if(StringUtils.startsWith(message, "226 ")) { // 下载完成
		} else if(StringUtils.startsWith(message, "502 ")) { // 不支持命令：FEAT
			this.unlockCommand();
		} else if(StringUtils.startsWith(message, "211-")) { // 服务器状态：扩展命令编码查询
			if(message.toUpperCase().contains(COMMAND_UTF8)) {
				this.charset = SystemConfig.CHARSET_UTF8;
				LOGGER.debug("FTP设置编码：{}", this.charset);
			}
			this.unlockCommand();
		} else if(StringUtils.startsWith(message, "227 ")) { // 进入被动模式：获取下载Socket的IP和端口
			release(); // 释放旧的资源
			final int opening = message.indexOf('(');
			final int closing = message.indexOf(')', opening + 1);
			if (closing > 0) {
				// 创建远程Socket
				final String data = message.substring(opening + 1, closing);
				final StringTokenizer tokenizer = new StringTokenizer(data, ",");
				final String host = tokenizer.nextToken() + "." + tokenizer.nextToken() + "." + tokenizer.nextToken() + "." + tokenizer.nextToken();
//				final int port = Integer.parseInt(tokenizer.nextToken()) * 256 + Integer.parseInt(tokenizer.nextToken());
				final int port = (Integer.parseInt(tokenizer.nextToken()) << 8) + Integer.parseInt(tokenizer.nextToken());
				try {
					this.socket = new Socket(host, port);
				} catch (IOException e) {
					LOGGER.error("打开FTP远程Socket异常", e);
				}
			}
		} else if(StringUtils.startsWith(message, "150 ")) { // 下载完成
			if(this.socket == null) {
				throw new NetException("请切换到被动模式");
			}
			try {
				this.inputStream = this.socket.getInputStream();
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
	 * 字符编码
	 */
	public String charset() {
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
		IoUtils.close(this.socket);
	}
	
	/**
	 * 锁定命令
	 */
	private void lockCommand() {
		synchronized (this.commandLock) {
			if(!this.commandLock.get()) {
				ThreadUtils.wait(this.commandLock, Duration.ofSeconds(5));
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
