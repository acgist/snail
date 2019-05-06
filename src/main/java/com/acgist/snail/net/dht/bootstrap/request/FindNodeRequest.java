package com.acgist.snail.net.dht.bootstrap.request;

import com.acgist.snail.net.dht.bootstrap.DhtService;
import com.acgist.snail.net.dht.bootstrap.Request;
import com.acgist.snail.net.dht.bootstrap.response.FindNodeResponse;
import com.acgist.snail.system.config.DhtConfig;
import com.acgist.snail.system.manager.NodeManager;

public class FindNodeRequest extends Request {

	private FindNodeRequest() {
		super(DhtService.getInstance().id(), DhtConfig.QType.find_node);
		this.put(DhtConfig.KEY_ID, NodeManager.getInstance().nodeId());
	}
	
	public static final FindNodeRequest newRequest(byte[] target) {
		final FindNodeRequest request = new FindNodeRequest();
		request.put(DhtConfig.KEY_TARGET, target);
		return request;
	}
	
	public String getTarget() {
		return getString(DhtConfig.KEY_TARGET);
	}

	public static final FindNodeResponse execute(Request request) {
		final FindNodeResponse response = FindNodeResponse.newInstance(request);
		final byte[] target = request.getBytes(DhtConfig.KEY_TARGET);
		final var nodes = NodeManager.getInstance().findNode(target);
		response.put(DhtConfig.KEY_NODES, writeNode(nodes));
		return response;
	}
	
}
