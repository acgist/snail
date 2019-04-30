package com.acgist.snail.net.dht.bootstrap.request;

import com.acgist.snail.net.dht.bootstrap.DhtService;
import com.acgist.snail.net.dht.bootstrap.Request;
import com.acgist.snail.system.config.DhtConfig;
import com.acgist.snail.system.manager.NodeManager;

public class GetPeersRequest extends Request {

	private GetPeersRequest() {
		super(DhtService.getInstance().id(), DhtConfig.QType.get_peers);
		this.put(DhtConfig.KEY_ID, NodeManager.getInstance().nodeId());
	}
	
	public static final GetPeersRequest newRequest(byte[] infoHash) {
		final GetPeersRequest request = new GetPeersRequest();
		request.put(DhtConfig.KEY_INFO_HASH, infoHash);
		return request;
	}
	
	public String getInfoHash() {
		return getString(DhtConfig.KEY_INFO_HASH);
	}
	
}
