package com.acgist.snail.net.torrent.dht.bootstrap.request;

import com.acgist.snail.config.DhtConfig;
import com.acgist.snail.net.torrent.dht.bootstrap.DhtRequest;
import com.acgist.snail.net.torrent.dht.bootstrap.response.PingResponse;

/**
 * <p>Ping</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class PingRequest extends DhtRequest {

	private PingRequest() {
		super(DhtConfig.QType.PING);
	}
	
	public static final PingRequest newRequest() {
		return new PingRequest();
	}

	public static final PingResponse execute(DhtRequest request) {
		return PingResponse.newInstance(request);
	}
	
}
