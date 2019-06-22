package com.acgist.snail.net.bt.dht.bootstrap.response;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.acgist.snail.net.bt.dht.bootstrap.NodeManager;
import com.acgist.snail.net.bt.dht.bootstrap.Request;
import com.acgist.snail.net.bt.dht.bootstrap.Response;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.system.config.DhtConfig;

/**
 * 返回最近的8个Node
 * 
 * @author acgist
 * @since 1.0.0
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
	
	/**
	 * 获取响应Node，同时加入到Node列表。
	 */
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
		NodeManager.getInstance().sortNodes();
		return list;
	}
	
}
