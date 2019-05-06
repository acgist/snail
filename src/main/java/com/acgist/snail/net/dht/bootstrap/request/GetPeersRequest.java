package com.acgist.snail.net.dht.bootstrap.request;

import java.nio.ByteBuffer;
import java.util.stream.Collectors;

import com.acgist.snail.net.dht.bootstrap.DhtService;
import com.acgist.snail.net.dht.bootstrap.Request;
import com.acgist.snail.net.dht.bootstrap.response.GetPeersResponse;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.config.DhtConfig;
import com.acgist.snail.system.manager.NodeManager;
import com.acgist.snail.system.manager.PeerSessionManager;
import com.acgist.snail.system.manager.TorrentSessionManager;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.StringUtils;

public class GetPeersRequest extends Request {

	private GetPeersRequest() {
		super(DhtService.getInstance().id(), DhtConfig.QType.get_peers);
		this.put(DhtConfig.KEY_ID, NodeManager.getInstance().nodeId());
	}
	
	public static final GetPeersRequest newRequest(byte[] infoHash) {
		final GetPeersRequest request = new GetPeersRequest();
		request.put(DhtConfig.KEY_INFO_HASH, infoHash);
		return request;
	}
	
	public String getInfoHash() {
		return getString(DhtConfig.KEY_INFO_HASH);
	}

	public static final GetPeersResponse execute(Request request) {
		final GetPeersResponse response = GetPeersResponse.newInstance(request);
		final byte[] infoHash = request.getBytes(DhtConfig.KEY_INFO_HASH);
		final String infoHashHex = StringUtils.hex(infoHash);
		final TorrentSession session = TorrentSessionManager.getInstance().torrentSession(infoHashHex);
		if(session != null) {
			final ByteBuffer buffer = ByteBuffer.allocate(6);
			final var list = PeerSessionManager.getInstance().list(infoHashHex);
			if(CollectionUtils.isNotEmpty(list)) {
				final var values = list.stream()
					.limit(NodeManager.GET_PEER_LENGTH)
					.map(peer -> {
						// TODO：测试
						buffer.putInt(NetUtils.encodeIpToInt(peer.host()));
						buffer.putShort(NetUtils.encodePort(peer.port()));
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
