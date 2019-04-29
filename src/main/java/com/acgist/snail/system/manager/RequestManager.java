package com.acgist.snail.system.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.dht.bootstrap.Request;

/**
 * DHT请求管理
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
	
	public void put(Request request) {
		if(request == null) {
			return;
		}
		final String id = request.getId();
		final Request old = requests.put(id, request);
		if(old != null) {
			LOGGER.warn("旧请求没有收到响应（剔除）");
		}
	}
	
}
