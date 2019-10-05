package com.acgist.snail.net.torrent.dht.bootstrap.request;

import com.acgist.snail.net.torrent.dht.bootstrap.DhtService;
import com.acgist.snail.net.torrent.dht.bootstrap.NodeManager;
import com.acgist.snail.net.torrent.dht.bootstrap.Request;
import com.acgist.snail.net.torrent.dht.bootstrap.response.FindNodeResponse;
import com.acgist.snail.system.config.DhtConfig;

/**
 * 查找Node
 * 
 * @author acgist
 * @since 1.0.0
 */
public class FindNodeRequest extends Request {

	private FindNodeRequest() {
		super(DhtService.getInstance().requestId(), DhtConfig.QType.find_node);
	}
	
	/**
	 * 创建请求
	 * 
	 * @param target InfoHash或者NodeId
	 */
	public static final FindNodeRequest newRequest(byte[] target) {
		final FindNodeRequest request = new FindNodeRequest();
		request.put(DhtConfig.KEY_TARGET, target);
		return request;
	}

	/**
	 * <p>处理请求</p>
	 */
	public static final FindNodeResponse execute(Request request) {
		final FindNodeResponse response = FindNodeResponse.newInstance(request);
		final byte[] target = request.getBytes(DhtConfig.KEY_TARGET);
		final var nodes = NodeManager.getInstance().findNode(target);
		response.put(DhtConfig.KEY_NODES, serializeNodes(nodes));
		return response;
	}
	
	public byte[] getTarget() {
		return getBytes(DhtConfig.KEY_TARGET);
	}

}
