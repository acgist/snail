package com.acgist.snail.net.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.TcpMessageHandler;
import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.ThreadUtils;

/**
 * FTP消息
 */
public class FtpMessageHandler extends TcpMessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(FtpMessageHandler.class);
	
	public static final String SPLIT = "\r\n"; // 处理粘包分隔符
	
	private boolean append = false; // 断点续传
	
	private String failMessage; // 错误信息
	
	private Socket socket; // Socket
	private InputStream inputStream; // 输入流
	
	private Object inputStreamLock = new Object(); // 获取流的等待锁
	
	private StringBuffer commandBuffer = new StringBuffer();
	
	public FtpMessageHandler() {
		super(SPLIT);
	}

	@Override
	public void onMessage(ByteBuffer attachment) {
		String command = IoUtils.readContent(attachment);
		if(command.contains(SPLIT)) {
			int index = command.indexOf(SPLIT);
			while(index >= 0) {
				commandBuffer.append(command.substring(0, index));
				oneMessage(commandBuffer.toString());
				commandBuffer.setLength(0);
				command = command.substring(index + SPLIT.length());
				index = command.indexOf(SPLIT);
			}
		}
		commandBuffer.append(command);
	}
	
	/**
	 * 处理单条消息
	 */
	private void oneMessage(String message) {
		LOGGER.debug("收到FTP响应：{}", message);
		if(StringUtils.startsWith(message, "530 ")) { // 登陆失败
			this.failMessage = "服务器需要登陆授权";
			this.close();
			this.unlockInputStream();
		} else if(StringUtils.startsWith(message, "550 ")) { // 文件不存在
			this.failMessage = "文件不存在";
			this.close();
			this.unlockInputStream();
		} else if(StringUtils.startsWith(message, "421 ")) { // Socket打开失败
			this.failMessage = "打开连接失败";
			this.close();
			this.unlockInputStream();
		} else if(StringUtils.startsWith(message, "350 ")) { // 端点续传
			this.append = true;
		} else if(StringUtils.startsWith(message, "220 ")) { // 退出系统
		} else if(StringUtils.startsWith(message, "226 ")) { // 下载完成
		} else if(StringUtils.startsWith(message, "227 ")) { // 进入被动模式：获取远程IP和端口
			release();
			int opening = message.indexOf('(');
			int closing = message.indexOf(')', opening + 1);
			if (closing > 0) {
				// 创建远程Socket
				final String data = message.substring(opening + 1, closing);
				final StringTokenizer tokenizer = new StringTokenizer(data, ",");
				final String host = tokenizer.nextToken() + "." + tokenizer.nextToken() + "." + tokenizer.nextToken() + "." + tokenizer.nextToken();
//				final int port = Integer.parseInt(tokenizer.nextToken()) * 256 + Integer.parseInt(tokenizer.nextToken());
				final int port = (Integer.parseInt(tokenizer.nextToken()) << 8) + Integer.parseInt(tokenizer.nextToken());
				try {
					socket = new Socket(host, port);
				} catch (IOException e) {
					LOGGER.error("打开FTP远程Socket异常", e);
				}
			}
		} else if(StringUtils.startsWith(message, "150 ")) { // 下载完成
			try {
				this.inputStream = socket.getInputStream();
				this.unlockInputStream();
			} catch (IOException e) {
				LOGGER.error("打开FTP远程输入流异常", e);
			}
		}
	}
	
	/**
	 * 是否支持断点续传
	 */
	public boolean append() {
		return this.append;
	}
	
	/**
	 * 获取错误信息
	 */
	public String failMessage() {
		return this.failMessage;
	}
	
	/**
	 * 获取输入流，阻塞线程
	 */
	public InputStream inputStream() {
		synchronized (this.inputStreamLock) {
			ThreadUtils.wait(this.inputStreamLock, Duration.ofSeconds(5));
		}
		if(this.inputStream == null && failMessage == null) {
			failMessage = "下载失败";
		}
		return this.inputStream;
	}
	
	/**
	 * 释放资源：注意释放FTP下载连接资源，不关闭命令通道
	 */
	public void release() {
		IoUtils.close(this.inputStream);
		IoUtils.close(this.socket);
	}

	@Override
	public void close() {
		this.release();
		super.close();
	}
	
	private void unlockInputStream() {
		synchronized (this.inputStreamLock) {
			this.inputStreamLock.notifyAll();
		}
	}
	
}
