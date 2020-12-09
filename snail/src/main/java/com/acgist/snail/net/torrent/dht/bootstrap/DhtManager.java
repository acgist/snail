package com.acgist.snail.net.torrent.dht.bootstrap;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.DhtConfig;
import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.utils.ArrayUtils;

/**
 * <p>DHT管理器</p>
 * <p>管理DHT请求</p>
 * 
 * @author acgist
 */
public final class DhtManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DhtManager.class);
	
	private static final DhtManager INSTANCE = new DhtManager();
	
	public static final DhtManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>DHT请求列表</p>
	 */
	private final List<DhtRequest> requests;
	
	private DhtManager() {
		this.requests = new LinkedList<>();
	}
	
	/**
	 * <p>注册DHT服务</p>
	 */
	public void register() {
		LOGGER.debug("注册DHT服务：定时任务");
		SystemThreadContext.timerFixedDelay(
			DhtConfig.DHT_REQUEST_CLEAN_INTERVAL,
			DhtConfig.DHT_REQUEST_CLEAN_INTERVAL,
			TimeUnit.MINUTES,
			() -> this.timeout() // 处理超时请求
		);
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
			final DhtRequest old = this.remove(request.getId());
			if(old != null) {
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
		DhtRequest request = null;
		synchronized (this.requests) {
			request = this.remove(response.getId());
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
		synchronized (this.requests) {
			DhtRequest request;
			final long timeout = DhtConfig.DHT_TIMEOUT;
			final long timestamp = System.currentTimeMillis();
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
			if(ArrayUtils.equals(id, request.getId())) {
				iterator.remove();
				return request;
			}
		}
		return null;
	}

}
