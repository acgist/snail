package com.acgist.snail.net.bt.utp.bootstrap;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.bt.utp.UtpMessageHandler;
import com.acgist.snail.system.context.SystemThreadContext;

/**
 * UTP服务
 * 
 * @author acgist
 * @since 1.1.0
 */
public class UtpService {
	
	private static final UtpService INSTANCE = new UtpService();
	
	private Map<String, UtpMessageHandler> utpMessageHandlers = new ConcurrentHashMap<>();
	
	private UtpService() {
		timer();
	}
	
	public static final UtpService getInstance() {
		return INSTANCE;
	}
	
	private int connectionId = 0;
	
	/**
	 * 定时任务，处理超时信息。
	 * TODO：时间优化
	 */
	private void timer() {
		SystemThreadContext.timerFixedDelay(1, 1, TimeUnit.SECONDS, () -> {
			this.utpMessageHandlers.values().forEach(handler -> {
				handler.wndControl();
			});
		});
	}
	
	/**
	 * 获取UTP消息处理器，如果已经存在直接返回。
	 * 
	 * @param connectionId 连接ID
	 * @param socketAddress 请求地址
	 */
	public UdpMessageHandler get(short connectionId, InetSocketAddress socketAddress) {
		final String key = buildKey(connectionId, socketAddress);
		UtpMessageHandler utpMessageHandler = this.utpMessageHandlers.get(key);
		if(utpMessageHandler != null) {
			return utpMessageHandler;
		}
		return new UtpMessageHandler(connectionId, socketAddress);
	}
	
	/**
	 * 添加消息处理器
	 */
	public void put(UtpMessageHandler utpMessageHandler) {
		this.utpMessageHandlers.put(utpMessageHandler.key(), utpMessageHandler);
	}
	
	/**
	 * 删除消息处理器
	 */
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
