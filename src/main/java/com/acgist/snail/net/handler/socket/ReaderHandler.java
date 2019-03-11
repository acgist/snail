package com.acgist.snail.net.handler.socket;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.handler.message.impl.ClientMessageHandler;
import com.acgist.snail.pojo.message.ClientMessage;
import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.StringUtils;

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
	 * TODO：粘包拆包
	 * @return 是否关闭
	 */
	private boolean doMessage(Integer result, ByteBuffer attachment) {
		boolean close = false;
		if (result == 0) {
			LOGGER.info("读取空消息");
		} else {
			String content = IoUtils.readContent(attachment);
			if(StringUtils.isEmpty(content)) {
				LOGGER.warn("读取消息内容为空关闭Socket");
				close = true;
				IoUtils.closeSocket(socket);
				return close;
			}
			ClientMessage message = ClientMessage.valueOf(content);
			if(message == null) {
				LOGGER.warn("读取消息格式错误关闭Socket：{}", content);
				close = true;
				IoUtils.closeSocket(socket);
				return close;
			}
			LOGGER.info("读取消息：{}", content);
			ClientMessageHandler messageHandler = new ClientMessageHandler(socket, message);
			close = messageHandler.execute();
		}
		return close;
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
