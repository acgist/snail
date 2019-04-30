package com.acgist.snail.system.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.dht.bootstrap.Request;
import com.acgist.snail.net.dht.bootstrap.Response;

/**
 * DHT请求管理
 * TODO：定时清除
 */
public class RequestManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RequestManager.class);

	private final Map<String, Request> requests;
	
	private RequestManager() {
		this.requests = new ConcurrentHashMap<>();
	}

	private static final RequestManager INSTANCE = new RequestManager();
	
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
		final String id = request.getIdHex();
		final Request old = requests.put(id, request);
		if(old != null) {
			LOGGER.warn("旧请求没有收到响应（剔除）");
		}
	}
	
	/**
	 * 获取请求，同时删除
	 */
	public Request take(Request request) {
		if(request == null) {
			return null;
		}
		return requests.remove(request.getIdHex());
	}

	/**
	 * 设置响应，唤醒请求线程
	 */
	public Request response(Response response) {
		if(response == null) {
			return null;
		}
		final Request request = requests.get(response.getIdHex());
		if(request == null) {
			return null;
		}
		request.setResponse(response);
		return request;
	}
	
}
