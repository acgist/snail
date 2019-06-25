package com.acgist.snail.net.torrent.dht.bootstrap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.config.DhtConfig;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.utils.ArrayUtils;

/**
 * <p>DHT请求管理器</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class RequestManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RequestManager.class);
	
	private RequestManager() {
		this.requests = new ArrayList<>();
	}

	private static final RequestManager INSTANCE = new RequestManager();
	
	static {
		LOGGER.debug("初始化DHT请求清空定时任务");
		SystemThreadContext.timerFixedDelay(5, 5, TimeUnit.MINUTES, () -> {
			RequestManager.getInstance().clear(); // 清除DHT超时请求
		});
	}
	
	private final List<Request> requests;
	
	public static final RequestManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 放入请求
	 */
	public void put(Request request) {
		if(request == null) {
			return;
		}
		final Request old = remove(request.getId());
		if(old != null) {
			LOGGER.warn("旧请求没有收到响应（剔除）");
		}
		this.requests.add(request);
	}
	
	/**
	 * 设置响应，删除响应，同时设置Node为可用状态。
	 */
	public Request response(Response response) {
		if(response == null) {
			return null;
		}
		NodeManager.getInstance().available(response);
		final Request request = remove(response.getId());
		if(request == null) {
			return null;
		}
		request.setResponse(response);
		return request;
	}
	
	/**
	 * 清理DHT过期请求。
	 */
	public void clear() {
		LOGGER.debug("清空DHT过期请求");
		synchronized (this.requests) {
			Request request;
			final long timeout = DhtConfig.TIMEOUT.toMillis();
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
	 * 移除请求
	 */
	private Request remove(byte[] id) {
		synchronized (this.requests) {
			Request request;
			final var iterator = this.requests.iterator();
			while(iterator.hasNext()) {
				request = iterator.next();
				if(ArrayUtils.equals(id, request.getId())) {
					iterator.remove();
					return request;
				}
			}
		}
		return null;
	}

}
