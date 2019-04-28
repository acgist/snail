package com.acgist.snail.net.dht.bootstrap.request;

import com.acgist.snail.net.dht.bootstrap.DhtService;
import com.acgist.snail.net.dht.bootstrap.NodeService;
import com.acgist.snail.net.dht.bootstrap.Request;
import com.acgist.snail.system.config.DhtConfig;

public class FindNodeRequest extends Request {

	private FindNodeRequest() {
		super(DhtService.getInstance().id(), DhtConfig.QType.find_node);
		this.put(DhtConfig.KEY_ID, NodeService.getInstance().nodeId());
	}
	
	public static final FindNodeRequest newRequest(String target) {
		final FindNodeRequest request = new FindNodeRequest();
		request.put(DhtConfig.KEY_TARGET, target);
		return request;
	}
	
	public String getTarget() {
		return getString(DhtConfig.KEY_TARGET);
	}
	
}
