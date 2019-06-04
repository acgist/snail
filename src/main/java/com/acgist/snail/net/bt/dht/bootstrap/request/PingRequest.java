package com.acgist.snail.net.bt.dht.bootstrap.request;

import com.acgist.snail.net.bt.dht.bootstrap.DhtService;
import com.acgist.snail.net.bt.dht.bootstrap.Request;
import com.acgist.snail.net.bt.dht.bootstrap.response.PingResponse;
import com.acgist.snail.system.config.DhtConfig;
import com.acgist.snail.system.manager.NodeManager;

/**
 * Ping
 * 
 * @author acgist
 * @since 1.0.0
 */
public class PingRequest extends Request {

	private PingRequest() {
		super(DhtService.getInstance().requestId(), DhtConfig.QType.ping);
		this.put(DhtConfig.KEY_ID, NodeManager.getInstance().nodeId());
	}
	
	public static final PingRequest newRequest() {
		return new PingRequest();
	}

	public static final PingResponse execute(Request request) {
		return PingResponse.newInstance(request);
	}
	
}
