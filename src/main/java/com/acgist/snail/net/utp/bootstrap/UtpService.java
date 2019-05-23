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
	
	private short connectionId = Short.MIN_VALUE;
	
	public UdpMessageHandler utpMessageHandler(final short connectionId, final InetSocketAddress socketAddress) {
		final String key = utpMessageHandlerKey(connectionId, socketAddress);
		UtpMessageHandler utpMessageHandler = this.utpMessageHandlers.get(key);
		if(utpMessageHandler != null) {
			return utpMessageHandler;
		}
		synchronized (this.utpMessageHandlers) {
			utpMessageHandler = this.utpMessageHandlers.get(key);
			if(utpMessageHandler != null) {
				return utpMessageHandler;
			}
			utpMessageHandler = new UtpMessageHandler();
			utpMessageHandler.socketAddress(socketAddress);
			utpMessageHandler.connectionId(connectionId);
			this.utpMessageHandlers.put(key, utpMessageHandler);
			return utpMessageHandler;
		}
	}
	
	private String utpMessageHandlerKey(Short connectionId, InetSocketAddress socketAddress) {
		return socketAddress.getHostString() + socketAddress.getPort() + connectionId;
	}

	/**
	 * 获取连接ID
	 */
	public short connectionId() {
		synchronized (this) {
			if(++connectionId >= Short.MAX_VALUE) { // 不能大于最大值
				connectionId = Short.MIN_VALUE;
			}
		}
		return connectionId;
	}
	
}
