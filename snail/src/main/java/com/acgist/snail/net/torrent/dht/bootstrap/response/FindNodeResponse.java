package com.acgist.snail.net.torrent.dht.bootstrap.response;

import java.util.List;

import com.acgist.snail.net.torrent.dht.bootstrap.DhtRequest;
import com.acgist.snail.net.torrent.dht.bootstrap.DhtResponse;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.system.config.DhtConfig;

/**
 * <p>查找Node</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class FindNodeResponse extends DhtResponse {

	private FindNodeResponse(byte[] t) {
		super(t);
	}
	
	private FindNodeResponse(DhtResponse response) {
		super(response.getT(), response.getY(), response.getR(), response.getE());
	}

	public static final FindNodeResponse newInstance(DhtResponse response) {
		return new FindNodeResponse(response);
	}

	public static final FindNodeResponse newInstance(DhtRequest request) {
		return new FindNodeResponse(request.getT());
	}
	
	/**
	 * <p>获取节点并加入系统</p>
	 * 
	 * @return 节点列表
	 */
	public List<NodeSession> getNodes() {
		final byte[] bytes = this.getBytes(DhtConfig.KEY_NODES);
		return deserializeNodes(bytes);
	}
	
}
