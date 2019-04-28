package com.acgist.snail.net.dht.bootstrap.request;

import com.acgist.snail.net.dht.bootstrap.DhtService;
import com.acgist.snail.net.dht.bootstrap.NodeService;
import com.acgist.snail.net.dht.bootstrap.Request;
import com.acgist.snail.system.config.DhtConfig;

public class AnnouncePeerRequest extends Request {

	private AnnouncePeerRequest() {
		super(DhtService.getInstance().id(), DhtConfig.QType.ping);
		this.put(DhtConfig.KEY_ID, NodeService.getInstance().nodeId());
	}
	
	public static final AnnouncePeerRequest newRequest() {
		final AnnouncePeerRequest request = new AnnouncePeerRequest();
		return request;
	}
	
}
