package com.acgist.snail.net.dht.bootstrap.response;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.acgist.snail.net.dht.bootstrap.Request;
import com.acgist.snail.net.dht.bootstrap.Response;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.system.config.DhtConfig;

/**
 * 如果有Peer，返回Peer，否者返回最近的Node
 * 返回8个Node或者100个Peer
 */
public class GetPeersResponse extends Response {

	public GetPeersResponse(Response response) {
		super(response.getT(), response.getY(), response.getR(), response.getE());
	}

	public static final GetPeersResponse newInstance(Response response) {
		return new GetPeersResponse(response);
	}

	// TODO：
	public static final FindNodeResponse newInstance(Request request) {
		final FindNodeResponse response = new FindNodeResponse(request.getT());
		return response;
	}
	
	public String getToken() {
		return getString(DhtConfig.KEY_TOKEN);
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
	
	public List<PeerSession> getPeers() {
		return this.getValues();
	}
	
	public List<PeerSession> getValues() {
		final byte[] bytes = this.getBytes(DhtConfig.KEY_VALUES);
		if(bytes == null) {
		}
		return null;
	}
	
	/**
	 * 是否含有Peers
	 */
	public boolean havePeers() {
		return get(DhtConfig.KEY_VALUES) != null;
	}
	
}
