package com.acgist.snail.net.message.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.message.AbstractMessageHandler;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * FTP消息
 */
public class FtpMessageHandler extends AbstractMessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(FtpMessageHandler.class);
	
	public static final String SPLIT = "\r\n"; // 处理粘包分隔符
	
	private boolean fail = false; // 失败
	private String failMessage; // 错误信息
	private StringBuffer contentBuffer = new StringBuffer();
	
	private Socket socket; // Socket
	private InputStream inputStream; // 输入流
	
	public FtpMessageHandler() {
		super(SPLIT);
	}

	@Override
	public boolean doMessage(Integer result, ByteBuffer attachment) {
		boolean doNext = true; // 是否继续处理消息
		if (result == 0) {
			LOGGER.info("读取空消息");
		} else {
			String content = IoUtils.readContent(attachment);
			if(content.contains(SPLIT)) {
				int index = content.indexOf(SPLIT);
				while(index >= 0) {
					contentBuffer.append(content.substring(0, index));
					doNext = oneMessage(contentBuffer.toString());
					contentBuffer.setLength(0);
					content = content.substring(index + SPLIT.length());
					index = content.indexOf(SPLIT);
				}
			}
			contentBuffer.append(content);
		}
		return doNext;
	}
	
	/**
	 * 处理单条消息
	 */
	private boolean oneMessage(String message) {
		if(fail) {
			return !fail;
		}
		if(StringUtils.startsWith(message, "530 ")) { // 登陆失败
			fail = true;
			failMessage = "服务器需要登陆授权";
		} else if(StringUtils.startsWith(message, "550 ")) { // 文件不存在
			fail = true;
			failMessage = "文件不存在";
		} else if(StringUtils.startsWith(message, "421 ")) { // Socket打开失败
			fail = true;
			failMessage = "打开连接失败";
		} else if(StringUtils.startsWith(message, "220 ")) { // 退出系统
		} else if(StringUtils.startsWith(message, "226 ")) { // 下载完成
		} else if(StringUtils.startsWith(message, "227 ")) { // 进入被动模式：获取远程IP和端口
			int opening = message.indexOf('(');
			int closing = message.indexOf(')', opening + 1);
			if (closing > 0) {
				// 创建远程Socket
				String data = message.substring(opening + 1, closing);
				StringTokenizer tokenizer = new StringTokenizer(data, ",");
				String ip = tokenizer.nextToken() + "." + tokenizer.nextToken() + "." + tokenizer.nextToken() + "." + tokenizer.nextToken();
				int port = Integer.parseInt(tokenizer.nextToken()) * 256 + Integer.parseInt(tokenizer.nextToken());
				try {
					socket = new Socket(ip, port);
				} catch (IOException e) {
					throw new NetException("打开远程Socket失败", e);
				}
			}
		} else if(StringUtils.startsWith(message, "150 ")) { // 下载完成
			try {
				this.inputStream = socket.getInputStream();
			} catch (IOException e) {
				throw new NetException("打开远程输入流失败", e);
			}
		}
		return !fail;
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
		while(this.inputStream == null) {
			Thread.yield();
			if(fail) {
				break;
			}
		}
		return this.inputStream;
	}
	
	/**
	 * 关闭线程
	 */
	public void close() {
		try {
			if(socket != null && !socket.isClosed()) {
				socket.shutdownInput();
				socket.shutdownOutput();
				socket.close();
			}
		} catch (IOException e) {
			LOGGER.error("关闭Socket异常", e);
		}
	}

}
