package com.acgist.snail.net.torrent.dht.bootstrap.response;

import com.acgist.snail.net.torrent.dht.bootstrap.DhtRequest;
import com.acgist.snail.net.torrent.dht.bootstrap.DhtResponse;

/**
 * <p>Ping</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class PingResponse extends DhtResponse {
	
	private PingResponse(byte[] t) {
		super(t);
	}

	private PingResponse(DhtResponse response) {
		super(response.getT(), response.getY(), response.getR(), response.getE());
	}

	public static final PingResponse newInstance(DhtResponse response) {
		return new PingResponse(response);
	}

	public static final PingResponse newInstance(DhtRequest request) {
		return new PingResponse(request.getT());
	}
	
}
