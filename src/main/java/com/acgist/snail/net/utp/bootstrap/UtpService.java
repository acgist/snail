package com.acgist.snail.net.utp.bootstrap;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.utp.UtpMessageHandler;

/**
 * 
 * @author acgist
 * @since 1.1.0
 */
public class UtpService {
	
	private static final UtpService INSTANCE = new UtpService();
	
	private Map<String, UtpMessageHandler> utpMessageHandlers = new ConcurrentHashMap<>();
	
	private UtpService() {
	}
	
	public static final UtpService getInstance() {
		return INSTANCE;
	}
	
	private int connectionId = 0;
	
	public UdpMessageHandler get(short connectionId, InetSocketAddress socketAddress) {
		final String key = buildKey(connectionId, socketAddress);
		UtpMessageHandler utpMessageHandler = this.utpMessageHandlers.get(key);
		if(utpMessageHandler != null) {
			return utpMessageHandler;
		}
		return new UtpMessageHandler(connectionId, socketAddress);
	}
	
	public void put(UtpMessageHandler utpMessageHandler) {
		this.utpMessageHandlers.put(utpMessageHandler.key(), utpMessageHandler);
	}
	
	public void remove(UtpMessageHandler utpMessageHandler) {
		this.utpMessageHandlers.remove(utpMessageHandler.key());
	}
	
	/**
	 * 获取连接ID
	 */
	public short connectionId() {
		synchronized (this) {
			return (short) connectionId++;
		}
	}
	
	/**
	 * 外网连入时key=地址+connectionId，本机key=connectionId
	 */
	public String buildKey(Short connectionId, InetSocketAddress socketAddress) {
		return socketAddress.getHostString() + socketAddress.getPort() + connectionId;
	}
	
}
