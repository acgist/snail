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
	
	public static final UtpService getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>UTP超时定时任务执行周期（秒）：{@value}</p>
	 */
	private static final int UTP_INTERVAL = 10;
	
	/**
	 * <p>连接ID</p>
	 */
	private int connectionId = 0;
	/**
	 * <p>UTP消息代理</p>
	 * <p>{@link #buildKey(short, InetSocketAddress)}=消息代理</p>
	 */
	private final Map<String, UtpMessageHandler> utpMessageHandlers = new ConcurrentHashMap<>();
	
	private UtpService() {
	}
	
	/**
	 * <p>注册UTP服务</p>
	 */
	public void register() {
		LOGGER.debug("注册UTP服务：定时任务");
		SystemThreadContext.timerFixedDelay(
			UTP_INTERVAL,
			UTP_INTERVAL,
			TimeUnit.SECONDS,
			() -> this.timeout()
		);
	}
	
	/**
	 * <p>获取连接ID</p>
	 * <p>每次获取递增</p>
	 * 
	 * @return 连接ID
	 */
	public short connectionId() {
		synchronized (this) {
			return (short) connectionId++;
		}
	}
	
	/**
	 * <p>获取UTP消息代理</p>
	 * <p>如果已经存在直接返回，否者创建并返回。</p>
	 * 
	 * @param connectionId 连接ID
	 * @param socketAddress 连接地址
	 * 
	 * @return UTP消息代理
	 */
	public UdpMessageHandler get(short connectionId, InetSocketAddress socketAddress) {
		final String key = this.buildKey(connectionId, socketAddress);
		final UtpMessageHandler utpMessageHandler = this.utpMessageHandlers.get(key);
		if(utpMessageHandler != null) {
			return utpMessageHandler;
		}
		return new UtpMessageHandler(connectionId, socketAddress);
	}
	
	/**
	 * <p>添加UTP消息代理</p>
	 * 
	 * @param utpMessageHandler UTP消息代理
	 */
	public void put(UtpMessageHandler utpMessageHandler) {
		synchronized (this.utpMessageHandlers) {
			this.utpMessageHandlers.put(utpMessageHandler.key(), utpMessageHandler);
		}
	}
	
	/**
	 * <p>删除UTP消息代理</p>
	 * 
	 * @param utpMessageHandler UTP消息代理
	 */
	public void remove(UtpMessageHandler utpMessageHandler) {
		synchronized (this.utpMessageHandlers) {
			this.utpMessageHandlers.remove(utpMessageHandler.key());
		}
	}
	
	/**
	 * <p>生成UTP消息代理key</p>
	 * <p>key = 地址 + 端口 + connectionId</p>
	 * 
	 * @param connectionId 连接ID
	 * @param socketAddress 请求地址
	 * 
	 * @return key
	 */
	public String buildKey(short connectionId, InetSocketAddress socketAddress) {
		return socketAddress.getHostString() + socketAddress.getPort() + connectionId;
	}
	
	/**
	 * <p>处理超时UTP消息</p>
	 * <p>如果消息代理可用：重新发送超时消息</p>
	 * <p>如果消息代理不可用：关闭消息代理</p>
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
					.collect(Collectors.toList()) // 转换List再关闭：防止关闭时删除消息代理产生异常
					.forEach(value -> value.close());
			} catch (Exception e) {
				LOGGER.error("处理超时UTP消息异常", e);
			}
		}
	}

}
