package com.acgist.snail.aio.handler;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.message.ClientMessage;
import com.acgist.snail.utils.AioUtils;
import com.acgist.snail.window.main.MainWindow;

import javafx.application.Platform;

/**
 * 消息读取
 */
public class ReaderHandler implements CompletionHandler<Integer, ByteBuffer> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReaderHandler.class);	
	
	private AsynchronousSocketChannel socket;

	public ReaderHandler(AsynchronousSocketChannel socket) {
		this.socket = socket;
	}

	@Override
	public void completed(Integer result, ByteBuffer attachment) {
		if(!doMessage(result, attachment)) {
			doReader();
		}
	}

	/**
	 * 处理消息
	 * @return 是否关闭
	 */
	private boolean doMessage(Integer result, ByteBuffer attachment) {
		boolean close = false;
		if (result == 0) {
			LOGGER.info("读取空消息");
		} else {
			String content = AioUtils.readContent(attachment);
			LOGGER.info("读取消息：{}", content);
			ClientMessage message = ClientMessage.valueOf(content);
			if(message.getType() == ClientMessage.Type.text) {
				send(ClientMessage.response(message.getBody()));
			} else if(message.getType() == ClientMessage.Type.notify) {
				Platform.runLater(() -> {
					MainWindow.show();
				});
			} else if(message.getType() == ClientMessage.Type.close) {
				AioUtils.close(socket);
				close = true;
			}
		}
		return close;
	}
	
	private void send(ClientMessage message) {
		ByteBuffer buffer = ByteBuffer.wrap(message.toBytes());
		synchronized (socket) { // 防止多线程抛异常
			socket.write(buffer, buffer, new WriterHandler());
		}
	}
	
	private void doReader() {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		socket.read(buffer, buffer, this);
	}
	
	@Override
	public void failed(Throwable exc, ByteBuffer attachment) {
		LOGGER.error("读取消息异常", exc);
	}

}
