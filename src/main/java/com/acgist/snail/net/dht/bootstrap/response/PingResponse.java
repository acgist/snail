package com.acgist.snail.net.dht.bootstrap.response;

import com.acgist.snail.net.dht.bootstrap.Request;
import com.acgist.snail.net.dht.bootstrap.Response;
import com.acgist.snail.system.config.DhtConfig;
import com.acgist.snail.system.manager.NodeManager;

public class PingResponse extends Response {

	public PingResponse(byte[] t) {
		super(t);
	}

	public PingResponse(Response response) {
		super(response.getT(), response.getY(), response.getR(), response.getE());
	}

	public static final PingResponse newInstance(Response response) {
		return new PingResponse(response);
	}

	public static final PingResponse newInstance(Request request) {
		final PingResponse response = new PingResponse(request.getT());
		response.put(DhtConfig.KEY_ID, NodeManager.getInstance().nodeId());
		return response;
	}
	
}
