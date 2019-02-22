package com.acgist.snail.module.handler.message;

import java.nio.channels.AsynchronousSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.message.ClientMessage;
import com.acgist.snail.utils.AioUtils;
import com.acgist.snail.window.main.MainWindow;

import javafx.application.Platform;

/**
 * 消息处理
 */
public class ClientMessageHandler extends ClientMessageSenderHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClientMessageHandler.class);
	
	private ClientMessage message;

	public ClientMessageHandler(AsynchronousSocketChannel socket, ClientMessage message) {
		this.socket = socket;
		this.message = message;
	}

	/**
	 * 处理消息
	 */
	public boolean execute() {
		boolean close = false;
		if(message.getType() == ClientMessage.Type.text) {
			textMessage();
		} else if(message.getType() == ClientMessage.Type.notify) {
			notifyMessage();
		} else if(message.getType() == ClientMessage.Type.close) {
			close = true;
			closeMessage();
		} else if(message.getType() == ClientMessage.Type.response) {
			responseMessage();
		} else {
			LOGGER.info("未适配的消息类型：{}", message.getType());
		}
		return close;
	}

	private void textMessage() {
		send(ClientMessage.response(message.getBody()));
	}
	
	private void notifyMessage() {
		Platform.runLater(() -> {
			MainWindow.getInstance().show();
		});
	}
	
	private void closeMessage() {
		AioUtils.close(null, null, socket);
	}
	
	private void responseMessage() {
		LOGGER.info("收到响应：{}", message.getBody());
	}
	
}
