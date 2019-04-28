package com.acgist.snail.net.dht.bootstrap.request;

import com.acgist.snail.net.dht.bootstrap.DhtService;
import com.acgist.snail.net.dht.bootstrap.NodeService;
import com.acgist.snail.net.dht.bootstrap.Request;
import com.acgist.snail.system.config.DhtConfig;

public class GetPeersRequest extends Request {

	private GetPeersRequest() {
		super(DhtService.getInstance().id(), DhtConfig.QType.ping);
		this.put(DhtConfig.KEY_ID, NodeService.getInstance().nodeId());
	}
	
	public static final GetPeersRequest newRequest(String infoHashHex) {
		final GetPeersRequest request = new GetPeersRequest();
		request.put(DhtConfig.KEY_INFO_HASH, infoHashHex);
		return request;
	}
	
	public String getInfoHash() {
		return getString(DhtConfig.KEY_INFO_HASH);
	}
	
}
