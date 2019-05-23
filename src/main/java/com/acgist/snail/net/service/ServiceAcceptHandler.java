package com.acgist.snail.net.service;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.acgist.snail.net.UdpAcceptHandler;
import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.dht.DhtMessageHandler;
import com.acgist.snail.net.utp.UtpMessageHandler;

/**
 * UDP服务消息处理：UTP、DHT
 * 
 * @author acgist
 * @since 1.1.0
 */
public class ServiceAcceptHandler extends UdpAcceptHandler {
	
	/**
	 * DHT消息开头字符
	 */
	private static final byte DHT_HEADER = 'd';
	
	private static final ServiceAcceptHandler INSTANCE = new ServiceAcceptHandler();
	
	private ServiceAcceptHandler() {
	}
	
	public static final ServiceAcceptHandler getInstance() {
		return INSTANCE;
	}
	
	private DhtMessageHandler dhtMessageHandler = new DhtMessageHandler();
	
	private Map<String, UtpMessageHandler> utpMessageHandlers = new ConcurrentHashMap<>();
	
	@Override
	public UdpMessageHandler messageHandler(ByteBuffer buffer, InetSocketAddress socketAddress) {
		buffer.flip();
		final byte header = buffer.get();
		buffer.position(buffer.limit()).limit(buffer.capacity());
		if(DHT_HEADER == header) {
			return dhtMessageHandler;
		} else {
			return utpMessageHandler(socketAddress);
		}
	}
	
	private UdpMessageHandler utpMessageHandler(InetSocketAddress socketAddress) {
		final String key = utpMessageHandlerKey(socketAddress);
		UtpMessageHandler utpMessageHandler = this.utpMessageHandlers.get(key);
		if(utpMessageHandler != null) {
			return utpMessageHandler;
		}
		synchronized (this.utpMessageHandlers) {
			utpMessageHandler = new UtpMessageHandler();
			utpMessageHandler.socketAddress(socketAddress);
			this.utpMessageHandlers.put(key, utpMessageHandler);
			return utpMessageHandler;
		}
	}
	
	private String utpMessageHandlerKey(InetSocketAddress socketAddress) {
		return socketAddress.getHostString() + socketAddress.getPort();
	}
	
}
