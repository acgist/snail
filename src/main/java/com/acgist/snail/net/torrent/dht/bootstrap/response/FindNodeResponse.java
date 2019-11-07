package com.acgist.snail.net.torrent.dht.bootstrap.response;

import java.util.List;

import com.acgist.snail.net.torrent.dht.bootstrap.Request;
import com.acgist.snail.net.torrent.dht.bootstrap.Response;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.system.config.DhtConfig;

/**
 * <p>查找Node</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class FindNodeResponse extends Response {

	private FindNodeResponse(byte[] t) {
		super(t);
	}
	
	private FindNodeResponse(Response response) {
		super(response.getT(), response.getY(), response.getR(), response.getE());
	}

	public static final FindNodeResponse newInstance(Response response) {
		return new FindNodeResponse(response);
	}

	public static final FindNodeResponse newInstance(Request request) {
		return new FindNodeResponse(request.getT());
	}
	
	public List<NodeSession> getNodes() {
		final byte[] bytes = this.getBytes(DhtConfig.KEY_NODES);
		return deserializeNodes(bytes);
	}
	
}
