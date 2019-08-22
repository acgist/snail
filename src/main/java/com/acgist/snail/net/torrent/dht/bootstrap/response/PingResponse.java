package com.acgist.snail.net.torrent.dht.bootstrap.response;

import com.acgist.snail.net.torrent.dht.bootstrap.Request;
import com.acgist.snail.net.torrent.dht.bootstrap.Response;

/**
 * Ping
 * 
 * @author acgist
 * @since 1.0.0
 */
public class PingResponse extends Response {
	
	private PingResponse(byte[] t) {
		super(t);
	}

	private PingResponse(Response response) {
		super(response.getT(), response.getY(), response.getR(), response.getE());
	}

	public static final PingResponse newInstance(Response response) {
		return new PingResponse(response);
	}

	public static final PingResponse newInstance(Request request) {
		return new PingResponse(request.getT());
	}
	
}
