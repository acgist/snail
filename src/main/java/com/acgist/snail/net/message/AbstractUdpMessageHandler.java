package com.acgist.snail.net.message;

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
public abstract class AbstractUdpMessageHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractUdpMessageHandler.class);

	/**
	 * 消息处理
	 */
	public abstract void doMessage(ByteBuffer buffer);
	
	/**
	 * 代理
	 */
	public void handle(DatagramChannel channel) throws IOException {
		ByteBuffer buffer = null;
		Selector selector = Selector.open();
		channel.register(selector, SelectionKey.OP_READ);
		while (true) {
			if(selector.select() > 0) {
				buffer = ByteBuffer.allocate(1024);
				final Set<SelectionKey> keys = selector.selectedKeys();
				final Iterator<SelectionKey> keysIterator = keys.iterator();
				while (keysIterator.hasNext()) {
					final SelectionKey selectedKey = keysIterator.next();
					keysIterator.remove();
					if (selectedKey.isValid() && selectedKey.isReadable()) {
						try {
							channel.receive(buffer);
						} catch (IOException e) {
							LOGGER.error("消息读取异常", e);
							continue;
						}
						doMessage(buffer);
					}
				}
			}
		}
	}
	
}
