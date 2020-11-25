package com.acgist.snail.net.torrent.dht.bootstrap.response;

import com.acgist.snail.net.torrent.dht.bootstrap.DhtRequest;
import com.acgist.snail.net.torrent.dht.bootstrap.DhtResponse;

/**
 * <p>Ping</p>
 * 
 * @author acgist
 */
public final class PingResponse extends DhtResponse {
	
	/**
	 * @param t 节点ID
	 */
	private PingResponse(byte[] t) {
		super(t);
	}

	/**
	 * @param response 响应
	 */
	private PingResponse(DhtResponse response) {
		super(response.getT(), response.getY(), response.getR(), response.getE());
	}

	/**
	 * <p>创建响应</p>
	 * 
	 * @param response 响应
	 * 
	 * @return 响应
	 */
	public static final PingResponse newInstance(DhtResponse response) {
		return new PingResponse(response);
	}

	/**
	 * <p>创建响应</p>
	 * 
	 * @param request 请求
	 * 
	 * @return 响应
	 */
	public static final PingResponse newInstance(DhtRequest request) {
		return new PingResponse(request.getT());
	}
	
}
