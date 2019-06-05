package com.acgist.snail.net.bt.dht.bootstrap.request;

import java.nio.ByteBuffer;
import java.util.stream.Collectors;

import com.acgist.snail.net.bt.dht.bootstrap.DhtService;
import com.acgist.snail.net.bt.dht.bootstrap.Request;
import com.acgist.snail.net.bt.dht.bootstrap.response.GetPeersResponse;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.config.DhtConfig;
import com.acgist.snail.system.manager.NodeManager;
import com.acgist.snail.system.manager.PeerManager;
import com.acgist.snail.system.manager.TorrentManager;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * 查找Peer
 * 
 * @author acgist
 * @since 1.0.0
 */
public class GetPeersRequest extends Request {

	private GetPeersRequest() {
		super(DhtService.getInstance().requestId(), DhtConfig.QType.get_peers);
		this.put(DhtConfig.KEY_ID, NodeManager.getInstance().nodeId());
	}
	
	/**
	 * 创建请求
	 * 
	 * @param infoHash infoHash
	 */
	public static final GetPeersRequest newRequest(byte[] infoHash) {
		final GetPeersRequest request = new GetPeersRequest();
		request.put(DhtConfig.KEY_INFO_HASH, infoHash);
		return request;
	}
	
	public String getInfoHash() {
		return getString(DhtConfig.KEY_INFO_HASH);
	}

	/**
	 * 将Peer和Node加入到列表
	 */
	public static final GetPeersResponse execute(Request request) {
		final GetPeersResponse response = GetPeersResponse.newInstance(request);
		final byte[] infoHash = request.getBytes(DhtConfig.KEY_INFO_HASH);
		final String infoHashHex = StringUtils.hex(infoHash);
		final TorrentSession session = TorrentManager.getInstance().torrentSession(infoHashHex);
		if(session != null) {
			final ByteBuffer buffer = ByteBuffer.allocate(6);
			final var list = PeerManager.getInstance().list(infoHashHex);
			if(CollectionUtils.isNotEmpty(list)) {
				final var values = list.stream()
					.filter(peer -> peer.available())
					.limit(DhtConfig.GET_PEER_LENGTH)
					.map(peer -> {
						buffer.putInt(NetUtils.encodeIpToInt(peer.host()));
						buffer.putShort(NetUtils.encodePort(peer.peerPort()));
						buffer.flip();
						return buffer.array();
					})
					.collect(Collectors.toList());
				response.put(DhtConfig.KEY_VALUES, values);
			}
		}
		final var nodes = NodeManager.getInstance().findNode(infoHash);
		response.put(DhtConfig.KEY_NODES, writeNode(nodes));
		return response;
	}
	
}
