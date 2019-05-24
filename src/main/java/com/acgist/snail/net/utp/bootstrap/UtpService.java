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
	
	public UdpMessageHandler utpMessageHandler(final short connectionId, final InetSocketAddress socketAddress) {
		final String key = utpMessageHandlerKey(connectionId, socketAddress);
		UtpMessageHandler utpMessageHandler = this.utpMessageHandlers.get(key);
		if(utpMessageHandler != null) {
			return utpMessageHandler;
		}
		return new UtpMessageHandler(connectionId, socketAddress);
	}
	
	public void putUtpMessageHandler(short connectionId, InetSocketAddress socketAddress, UtpMessageHandler utpMessageHandler) {
		final String key = utpMessageHandlerKey(connectionId, socketAddress);
		this.utpMessageHandlers.put(key, utpMessageHandler);
	}
	
	/**
	 * 外网连入时key=地址+connectionId，本机key=connectionId
	 */
	private String utpMessageHandlerKey(Short connectionId, InetSocketAddress socketAddress) {
		return socketAddress.getHostString() + socketAddress.getPort() + connectionId;
	}

	/**
	 * 获取连接ID
	 */
	public short connectionId() {
		synchronized (this) {
			return (short) connectionId++;
		}
	}
	
}
