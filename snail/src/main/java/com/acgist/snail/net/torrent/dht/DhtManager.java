package com.acgist.snail.net.torrent.dht;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.IManager;
import com.acgist.snail.config.DhtConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.NumberUtils;

/**
 * <p>DHT管理器</p>
 * <p>管理DHT请求</p>
 * 
 * @author acgist
 */
public final class DhtManager implements IManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DhtManager.class);
	
	private static final DhtManager INSTANCE = new DhtManager();
	
	public static final DhtManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>Token长度：{@value}</p>
	 */
	private static final int TOKEN_LENGTH = 8;
	/**
	 * <p>消息ID最小值：{@value}</p>
	 */
	private static final int MIN_ID_VALUE = 0;
	/**
	 * <p>消息ID最大值：{@value}</p>
	 */
	private static final int MAX_ID_VALUE = 2 << 15;

	/**
	 * <p>当前客户端的Token</p>
	 */
	private final byte[] token;
	/**
	 * <p>消息ID</p>
	 */
	private int requestId = MIN_ID_VALUE;
	/**
	 * <p>DHT请求列表</p>
	 */
	private final List<DhtRequest> requests;
	
	/**
	 * <p>禁止创建实例</p>
	 */
	private DhtManager() {
		this.token = this.buildToken();
		this.requests = new LinkedList<>();
		this.register();
	}
	
	/**
	 * <p>注册DHT服务</p>
	 */
	private void register() {
		LOGGER.debug("注册DHT服务：定时任务");
		SystemThreadContext.timerFixedDelay(
			DhtConfig.DHT_REQUEST_CLEAN_INTERVAL,
			DhtConfig.DHT_REQUEST_CLEAN_INTERVAL,
			TimeUnit.MINUTES,
			this::timeout
		);
	}
	
	/**
	 * <p>获取系统Token</p>
	 * 
	 * @return 系统Token
	 */
	public byte[] token() {
		return this.token;
	}
	
	/**
	 * <p>生成系统Token</p>
	 * 
	 * @return 系统Token
	 */
	private byte[] buildToken() {
		LOGGER.debug("生成系统Token");
		final byte[] token = new byte[TOKEN_LENGTH];
		final byte[] tokens = (SystemConfig.LETTER + SystemConfig.LETTER_UPPER + SystemConfig.DIGIT).getBytes();
		final int length = tokens.length;
		final Random random = NumberUtils.random();
		for (int index = 0; index < TOKEN_LENGTH; index++) {
			token[index] = tokens[random.nextInt(length)];
		}
		return token;
	}
	
	/**
	 * <p>生成一个两字节的消息ID</p>
	 * 
	 * @return 消息ID
	 */
	public byte[] buildRequestId() {
		final byte[] bytes = new byte[2];
		synchronized (this) {
			if(++this.requestId >= MAX_ID_VALUE) {
				this.requestId = MIN_ID_VALUE;
			}
			bytes[0] = (byte) ((this.requestId >> 8) & 0xFF);
			bytes[1] = (byte) (this.requestId & 0xFF);
		}
		return bytes;
	}
	
	/**
	 * <p>放入请求</p>
	 * <p>如果请求列表中有相同ID的请求删除旧请求</p>
	 * 
	 * @param request 请求
	 */
	public void request(DhtRequest request) {
		if(request == null) {
			return;
		}
		synchronized (this.requests) {
			final DhtRequest oldRequest = this.remove(request.getT());
			if(oldRequest != null) {
				LOGGER.warn("旧DHT请求没有收到响应（删除）");
			}
			this.requests.add(request);
		}
	}
	
	/**
	 * <p>设置响应</p>
	 * <p>删除响应对应的请求并返回，同时设置对应节点为可用状态。</p>
	 * 
	 * @param response 响应
	 * 
	 * @return 请求
	 */
	public DhtRequest response(DhtResponse response) {
		if(response == null) {
			return null;
		}
		// 设置节点为可用状态
		NodeManager.getInstance().available(response.getNodeId());
		DhtRequest request;
		synchronized (this.requests) {
			request = this.remove(response.getT());
		}
		if(request != null) {
			request.setResponse(response);
		}
		return request;
	}
	
	/**
	 * <p>处理DHT超时请求</p>
	 */
	private void timeout() {
		LOGGER.debug("处理DHT超时请求");
		final long timeout = DhtConfig.DHT_TIMEOUT;
		final long timestamp = System.currentTimeMillis();
		synchronized (this.requests) {
			DhtRequest request;
			final var iterator = this.requests.iterator();
			while(iterator.hasNext()) {
				request = iterator.next();
				if(timestamp - request.getTimestamp() > timeout) {
					iterator.remove();
				}
			}
		}
	}
	
	/**
	 * <p>移除并返回请求</p>
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
			if(ArrayUtils.equals(id, request.getT())) {
				iterator.remove();
				return request;
			}
		}
		return null;
	}

}
