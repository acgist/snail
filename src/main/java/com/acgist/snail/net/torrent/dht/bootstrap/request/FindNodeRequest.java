package com.acgist.snail.net.torrent.dht.bootstrap.request;

import com.acgist.snail.net.torrent.dht.bootstrap.NodeManager;
import com.acgist.snail.net.torrent.dht.bootstrap.DhtRequest;
import com.acgist.snail.net.torrent.dht.bootstrap.response.FindNodeResponse;
import com.acgist.snail.system.config.DhtConfig;

/**
 * <p>查找Node</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class FindNodeRequest extends DhtRequest {

	private FindNodeRequest() {
		super(DhtConfig.QType.FIND_NODE);
	}
	
	/**
	 * <p>创建请求</p>
	 * 
	 * @param target NodeId
	 * 
	 * @return 请求
	 */
	public static final FindNodeRequest newRequest(byte[] target) {
		final FindNodeRequest request = new FindNodeRequest();
		request.put(DhtConfig.KEY_TARGET, target);
		return request;
	}

	/**
	 * <p>处理请求</p>
	 * 
	 * @param request 请求
	 * 
	 * @return 响应
	 */
	public static final FindNodeResponse execute(DhtRequest request) {
		final FindNodeResponse response = FindNodeResponse.newInstance(request);
		final byte[] target = request.getBytes(DhtConfig.KEY_TARGET);
		final var nodes = NodeManager.getInstance().findNode(target);
		response.put(DhtConfig.KEY_NODES, serializeNodes(nodes));
		return response;
	}
	
	/**
	 * <p>获取NodeId或者InfoHash</p>
	 * 
	 * @return NodeId或者InfoHash
	 */
	public byte[] getTarget() {
		return getBytes(DhtConfig.KEY_TARGET);
	}

}
