package com.acgist.snail.net.torrent.utp.bootstrap;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.torrent.utp.UtpMessageHandler;
import com.acgist.snail.system.context.SystemThreadContext;

/**
 * <p>UTP Service</p>
 * <p>管理UTP消息代理</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class UtpService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UtpService.class);
	
	private static final UtpService INSTANCE = new UtpService();
	
	/**
	 * UTP超时定时任务
	 */
	private static final int UTP_INTERVAL = 10;
	
	/**
	 * 连接ID：每次获取+1
	 */
	private int connectionId = 0;
	/**
	 * UTP消息代理
	 */
	private final Map<String, UtpMessageHandler> utpMessageHandlers = new ConcurrentHashMap<>();
	
	private UtpService() {
	}
	
	public static final UtpService getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>注册UTP服务</p>
	 */
	public void register() {
		LOGGER.debug("注册UTP服务：定时任务");
		SystemThreadContext.timerFixedDelay(UTP_INTERVAL, UTP_INTERVAL, TimeUnit.SECONDS, () -> {
			this.timeout();
		});
	}
	
	/**
	 * @return 连接ID
	 */
	public short connectionId() {
		synchronized (this) {
			return (short) connectionId++;
		}
	}
	
	/**
	 * <p>获取UTP消息代理</p>
	 * <p>如果已经存在直接返回，否者创建。</p>
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
	 * 添加消息代理
	 */
	public void put(UtpMessageHandler utpMessageHandler) {
		synchronized (this.utpMessageHandlers) {
			this.utpMessageHandlers.put(utpMessageHandler.key(), utpMessageHandler);
		}
	}
	
	/**
	 * 删除消息代理
	 */
	public void remove(UtpMessageHandler utpMessageHandler) {
		synchronized (this.utpMessageHandlers) {
			this.utpMessageHandlers.remove(utpMessageHandler.key());
		}
	}
	
	/**
	 * key = 地址 + 端口 + connectionId
	 */
	public String buildKey(Short connectionId, InetSocketAddress socketAddress) {
		return socketAddress.getHostString() + socketAddress.getPort() + connectionId;
	}
	
	/**
	 * <p>处理超时UTP消息</p>
	 */
	private void timeout() {
		LOGGER.debug("处理超时UTP消息");
		synchronized (this.utpMessageHandlers) {
			try {
				this.utpMessageHandlers.values().stream()
					.filter(handler -> {
						if(handler.available()) { // 消息代理可用：重试
							handler.timeoutRetry();
							return false;
						} else { // 消息代理不可用：关闭
							return true;
						}
					})
					.collect(Collectors.toList())
					.forEach(value -> value.close());
			} catch (Exception e) {
				LOGGER.error("处理超时UTP消息异常", e);
			}
		}
	}

}
