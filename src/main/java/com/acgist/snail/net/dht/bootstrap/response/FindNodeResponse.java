package com.acgist.snail.net.dht.bootstrap.response;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.acgist.snail.net.dht.bootstrap.Request;
import com.acgist.snail.net.dht.bootstrap.Response;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.system.config.DhtConfig;
import com.acgist.snail.system.manager.NodeManager;

/**
 * 8个最近的节点
 */
public class FindNodeResponse extends Response {

	private FindNodeResponse(byte[] t) {
		super(t);
		this.put(DhtConfig.KEY_ID, NodeManager.getInstance().nodeId());
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
		if(bytes == null) {
			return null;
		}
		final ByteBuffer buffer = ByteBuffer.wrap(bytes);
		final List<NodeSession> list = new ArrayList<>();
		while(true) {
			final var session = readNode(buffer);
			if(session == null) {
				break;
			}
			list.add(session);
		}
		return list;
	}
	
}
