package com.acgist.snail.net;

import java.io.IOException;
import java.net.InetSocketAddress;
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
 * 
 * @author acgist
 * @since 1.0.0
 */
public abstract class UdpMessageHandler extends UdpSender {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UdpMessageHandler.class);

	private static final int BUFFER_SIZE = 10 * 1024;
	
	/**
	 * 消息处理
	 */
	public abstract void onMessage(ByteBuffer buffer, InetSocketAddress address);
	
	/**
	 * 代理Channel
	 */
	public void handle(DatagramChannel channel) {
		this.channel = channel;
	}

	/**
	 * 循环读取消息
	 */
	public void loopMessage() throws IOException {
		final Selector selector = Selector.open();
		if(channel == null || !channel.isOpen()) {
			return;
		}
		channel.register(selector, SelectionKey.OP_READ);
		while (channel.isOpen()) {
			try {
				if(selector.select() > 0) {
					final Set<SelectionKey> keys = selector.selectedKeys();
					final Iterator<SelectionKey> keysIterator = keys.iterator();
					while (keysIterator.hasNext()) {
						final SelectionKey selectedKey = keysIterator.next();
						keysIterator.remove();
						if (selectedKey.isValid() && selectedKey.isReadable()) {
							final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
							final InetSocketAddress address = (InetSocketAddress) channel.receive(buffer);
							try {
								onMessage(buffer, address);
							} catch (Exception e) {
								LOGGER.error("UDP消息处理异常", e);
							}
						}
					}
				}
			} catch (IOException e) {
				LOGGER.error("UDP消息读取异常", e);
				continue;
			}
		}
	}

}
