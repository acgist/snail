package com.acgist.snail.net.dht.bootstrap.response;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.acgist.snail.net.dht.bootstrap.Request;
import com.acgist.snail.net.dht.bootstrap.Response;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.system.config.DhtConfig;
import com.acgist.snail.utils.NetUtils;

public class FindNodeResponse extends Response {

	public FindNodeResponse(byte[] t) {
		super(t);
	}
	
	public FindNodeResponse(Response response) {
		super(response.getT(), response.getY(), response.getR(), response.getE());
	}

	public static final FindNodeResponse newInstance(Response response) {
		return new FindNodeResponse(response);
	}

	public static final FindNodeResponse newInstance(Request request) {
		final FindNodeResponse response = new FindNodeResponse(request.getT());
		response.put(DhtConfig.KEY_NODES, "1234");
		return response;
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
	
	private NodeSession readNode(ByteBuffer buffer) {
		if(buffer.hasRemaining()) {
			final byte[] id = new byte[20];
			buffer.get(id);
			final String host = NetUtils.decodeIntToIp(buffer.getInt());
			final int port = NetUtils.decodePort(buffer.getShort());
			final NodeSession nodeSession = NodeSession.newInstance(id, host, port);
			return nodeSession;
		}
		return null;
	}
	
}
