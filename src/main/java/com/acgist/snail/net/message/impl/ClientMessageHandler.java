package com.acgist.snail.net.message.impl;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.message.AbstractMessageHandler;
import com.acgist.snail.pojo.message.ClientMessage;
import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.window.main.MainWindow;

import javafx.application.Platform;

/**
 * 客户端消息
 */
public class ClientMessageHandler extends AbstractMessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClientMessageHandler.class);
	
	/**
	 * 客户端消息
	 */
	@Override
	public boolean doMessage(AsynchronousSocketChannel socket, Integer result, ByteBuffer attachment) {
		boolean doNext = true; // 是否继续处理消息
		if (result == 0) {
			LOGGER.info("读取空消息");
		} else {
			String content = IoUtils.readContent(attachment);
			if(StringUtils.isEmpty(content)) {
				LOGGER.warn("读取消息内容为空关闭Socket");
				doNext = false;
				IoUtils.closeSocket(socket);
				return doNext;
			}
			ClientMessage message = ClientMessage.valueOf(content);
			if(message == null) {
				LOGGER.warn("读取消息格式错误关闭Socket：{}", content);
				doNext = false;
				IoUtils.closeSocket(socket);
				return doNext;
			}
			LOGGER.info("读取消息：{}", content);
			doNext = !this.execute(socket, message);
		}
		return doNext;
	};
	
	/**
	 * 处理消息
	 * @return 是否关闭socket：true-关闭；false-继续
	 */
	private boolean execute(AsynchronousSocketChannel socket, ClientMessage message) {
		boolean close = false; // 是否关闭
		if(message.getType() == ClientMessage.Type.text) { // 文本信息：直接原因返回
			send(socket, ClientMessage.response(message.getBody()));
		} else if(message.getType() == ClientMessage.Type.notify) { // 唤醒主窗口
			Platform.runLater(() -> {
				MainWindow.getInstance().show();
			});
		} else if(message.getType() == ClientMessage.Type.close) { // 关闭
			close = true;
			IoUtils.closeSocket(socket);
		} else if(message.getType() == ClientMessage.Type.response) { // 响应内容
			LOGGER.info("收到响应：{}", message.getBody());
		} else {
			LOGGER.warn("未适配的消息类型：{}", message.getType());
		}
		return close;
	}

}
