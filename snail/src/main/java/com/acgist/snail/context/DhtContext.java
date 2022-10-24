package com.acgist.snail.context;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.acgist.snail.IContext;
import com.acgist.snail.config.DhtConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.torrent.dht.DhtRequest;
import com.acgist.snail.net.torrent.dht.DhtResponse;
import com.acgist.snail.utils.NumberUtils;

/**
 * DHT上下文
 * 
 * @author acgist
 */
public final class DhtContext implements IContext {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DhtContext.class);
	
	private static final DhtContext INSTANCE = new DhtContext();
	
	public static final DhtContext getInstance() {
		return INSTANCE;
	}
	
	/**
	 * Token长度：{@value}
	 */
	private static final int TOKEN_LENGTH = 8;
	/**
	 * Token字符：{@value}
	 */
	private static final String TOKEN_CHARACTER =
		"0123456789" +
		"abcdefghijklmnopqrstuvwxyz" +
		"ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	/**
	 * Token
	 */
	private final byte[] token;
	/**
	 * 消息ID
	 */
	private short requestId = Short.MIN_VALUE;
	/**
	 * DHT请求列表
	 */
	private final List<DhtRequest> requests;
	
	private DhtContext() {
		this.token = this.buildToken();
		this.requests = new LinkedList<>();
		SystemThreadContext.scheduledAtFixedDelay(
			DhtConfig.DHT_REQUEST_TIMEOUT_INTERVAL,
			DhtConfig.DHT_REQUEST_TIMEOUT_INTERVAL,
			TimeUnit.MINUTES,
			this::timeout
		);
	}
	
	/**
	 * @return Token
	 */
	public byte[] token() {
		return this.token;
	}
	
	/**
	 * @return Token
	 */
	private byte[] buildToken() {
		final byte[] token = new byte[TOKEN_LENGTH];
		final byte[] bytes = TOKEN_CHARACTER.getBytes();
		final int length = bytes.length;
		final Random random = NumberUtils.random();
		for (int index = 0; index < TOKEN_LENGTH; index++) {
			token[index] = bytes[random.nextInt(length)];
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("生成DHT的Token：{}", new String(token));
		}
		return token;
	}
	
	/**
	 * @return 消息ID
	 */
	public byte[] buildRequestId() {
		synchronized (this) {
			return NumberUtils.shortToBytes(this.requestId++);
		}
	}
	
	/**
	 * 放入请求
	 * 
	 * @param request 请求
	 */
	public void request(DhtRequest request) {
		if (request == null) {
			return;
		}
		synchronized (this.requests) {
			// 删除旧的请求
			final DhtRequest oldRequest = this.remove(request.getT());
			if (oldRequest != null) {
				LOGGER.debug("删除没有收到响应的DHT请求：{}", oldRequest);
			}
			this.requests.add(request);
		}
	}
	
	/**
	 * 设置响应
	 * 
	 * @param response 响应
	 * 
	 * @return 响应对应的请求
	 */
	public DhtRequest response(DhtResponse response) {
		if (response == null) {
			return null;
		}
		// 设置节点可用状态
		NodeContext.getInstance().available(response.getNodeId());
		DhtRequest request;
		synchronized (this.requests) {
			// 删除请求
			request = this.remove(response.getT());
		}
		if (request != null) {
			// 设置响应
			request.setResponse(response);
		}
		return request;
	}
	
	/**
	 * 处理DHT超时请求
	 */
	private void timeout() {
		final long timeout = SystemConfig.RECEIVE_TIMEOUT_MILLIS;
		final long timestamp = System.currentTimeMillis();
		synchronized (this.requests) {
			final int oldSize = this.requests.size();
			DhtRequest request;
			final var iterator = this.requests.iterator();
			while(iterator.hasNext()) {
				request = iterator.next();
				if(timestamp - request.getTimestamp() > timeout) {
					iterator.remove();
				}
			}
			final int newSize = this.requests.size();
			LOGGER.debug("""
				处理DHT超时请求
				处理之前请求数量：{}
				处理之后请求数量：{}
				""", oldSize, newSize);
		}
	}
	
	/**
	 * 删除并返回删除的请求
	 * 注意线程安全
	 * 
	 * @param id 消息ID
	 * 
	 * @return 请求
	 */
	private DhtRequest remove(byte[] id) {
		DhtRequest request;
		final var iterator = this.requests.iterator();
		while(iterator.hasNext()) {
			request = iterator.next();
			if(Arrays.equals(id, request.getT())) {
				iterator.remove();
				return request;
			}
		}
		return null;
	}

}
