package com.acgist.snail.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UDP消息
 */
public abstract class UdpMessageHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UdpMessageHandler.class);

	private DatagramChannel channel;
	
	/**
	 * 消息处理
	 */
	public abstract void onMessage(ByteBuffer buffer);
	
	/**
	 * 代理
	 */
	public void handle(DatagramChannel channel) throws IOException {
		this.channel = channel;
		loopMessage();
	}

	/**
	 * 循环读取消息
	 */
	private void loopMessage() throws IOException {
		final Selector selector = Selector.open();
		if(channel == null || !channel.isOpen()) {
			return;
		}
		channel.register(selector, SelectionKey.OP_READ);
		while (channel.isOpen()) {
			try {
				if(selector.select() > 0) {
					final ByteBuffer buffer = ByteBuffer.allocate(1024);
					final Set<SelectionKey> keys = selector.selectedKeys();
					final Iterator<SelectionKey> keysIterator = keys.iterator();
					while (keysIterator.hasNext()) {
						final SelectionKey selectedKey = keysIterator.next();
						keysIterator.remove();
						if (selectedKey.isValid() && selectedKey.isReadable()) {
							channel.receive(buffer);
							onMessage(buffer);
						}
					}
				}
			} catch (IOException e) {
				LOGGER.error("消息读取异常", e);
				continue;
			}
		}
	}

}
