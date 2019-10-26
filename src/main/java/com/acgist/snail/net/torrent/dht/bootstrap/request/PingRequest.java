package com.acgist.snail.net.torrent.dht.bootstrap.request;

import com.acgist.snail.net.torrent.dht.bootstrap.DhtService;
import com.acgist.snail.net.torrent.dht.bootstrap.Request;
import com.acgist.snail.net.torrent.dht.bootstrap.response.PingResponse;
import com.acgist.snail.system.config.DhtConfig;

/**
 * Ping
 * 
 * @author acgist
 * @since 1.0.0
 */
public class PingRequest extends Request {

	private PingRequest() {
		super(DhtService.getInstance().requestId(), DhtConfig.QType.PING);
	}
	
	public static final PingRequest newRequest() {
		return new PingRequest();
	}

	public static final PingResponse execute(Request request) {
		return PingResponse.newInstance(request);
	}
	
}
