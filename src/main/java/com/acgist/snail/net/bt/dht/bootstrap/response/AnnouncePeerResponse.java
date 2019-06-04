package com.acgist.snail.net.bt.dht.bootstrap.response;

import com.acgist.snail.net.bt.dht.bootstrap.Request;
import com.acgist.snail.net.bt.dht.bootstrap.Response;
import com.acgist.snail.system.config.DhtConfig;
import com.acgist.snail.system.manager.NodeManager;

/**
 * 声明
 * 
 * @author acgist
 * @since 1.0.0
 */
public class AnnouncePeerResponse extends Response {

	private AnnouncePeerResponse(byte[] t) {
		super(t);
		this.put(DhtConfig.KEY_ID, NodeManager.getInstance().nodeId());
	}
	
	private AnnouncePeerResponse(Response response) {
		super(response.getT(), response.getY(), response.getR(), response.getE());
	}

	public static final AnnouncePeerResponse newInstance(Response response) {
		return new AnnouncePeerResponse(response);
	}

	public static final AnnouncePeerResponse newInstance(Request request) {
		return new AnnouncePeerResponse(request.getT());
	}
	
}
