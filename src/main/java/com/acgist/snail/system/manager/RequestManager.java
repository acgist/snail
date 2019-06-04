package com.acgist.snail.system.manager;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.bt.dht.bootstrap.Request;
import com.acgist.snail.net.bt.dht.bootstrap.Response;
import com.acgist.snail.utils.ArrayUtils;

/**
 * DHT请求管理
 * TODO：定时清除
 */
public class RequestManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RequestManager.class);

	private final List<Request> requests;
	
	private RequestManager() {
		this.requests = new ArrayList<>();
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
		final Request old = remove(request.getId());
		if(old != null) {
			LOGGER.warn("旧请求没有收到响应（剔除）");
		}
		requests.add(request);
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
	
	private Request remove(byte[] id) {
		synchronized (this.requests) {
			final var iterator = this.requests.iterator();
			Request request;
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
