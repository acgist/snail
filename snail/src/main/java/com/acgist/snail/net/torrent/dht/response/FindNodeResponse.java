package com.acgist.snail.net.torrent.dht.response;

import java.util.List;

import com.acgist.snail.config.DhtConfig;
import com.acgist.snail.net.torrent.dht.DhtRequest;
import com.acgist.snail.net.torrent.dht.DhtResponse;
import com.acgist.snail.net.torrent.dht.NodeSession;

/**
 * <p>查找Node</p>
 * 
 * @author acgist
 */
public final class FindNodeResponse extends DhtResponse {

	/**
	 * @param t 节点ID
	 */
	private FindNodeResponse(byte[] t) {
		super(t);
	}
	
	/**
	 * @param response 响应
	 */
	private FindNodeResponse(DhtResponse response) {
		super(response.getT(), response.getY(), response.getR(), response.getE());
	}

	/**
	 * <p>新建响应</p>
	 * 
	 * @param request 请求
	 * 
	 * @return 响应
	 */
	public static final FindNodeResponse newInstance(DhtRequest request) {
		return new FindNodeResponse(request.getT());
	}
	
	/**
	 * <p>新建响应</p>
	 * 
	 * @param response 响应
	 * 
	 * @return 响应
	 */
	public static final FindNodeResponse newInstance(DhtResponse response) {
		return new FindNodeResponse(response);
	}
	
	/**
	 * <p>获取节点列表</p>
	 * 
	 * @return 节点列表
	 */
	public List<NodeSession> getNodes() {
		// TODO：want
		return this.deserializeNodes(DhtConfig.KEY_NODES);
	}
	
}
