package com.acgist.snail.net.torrent.dht.bootstrap.request;

import java.nio.ByteBuffer;
import java.util.stream.Collectors;

import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.dht.bootstrap.DhtService;
import com.acgist.snail.net.torrent.dht.bootstrap.NodeManager;
import com.acgist.snail.net.torrent.dht.bootstrap.Request;
import com.acgist.snail.net.torrent.dht.bootstrap.response.GetPeersResponse;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerManager;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.config.DhtConfig;
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
		super(DhtService.getInstance().requestId(), DhtConfig.QType.GET_PEERS);
	}
	
	/**
	 * 创建请求
	 * 
	 * @param infoHash InfoHash
	 */
	public static final GetPeersRequest newRequest(byte[] infoHash) {
		final GetPeersRequest request = new GetPeersRequest();
		request.put(DhtConfig.KEY_INFO_HASH, infoHash);
		return request;
	}

	/**
	 * <p>处理请求</p>
	 */
	public static final GetPeersResponse execute(Request request) {
		final GetPeersResponse response = GetPeersResponse.newInstance(request);
		final byte[] infoHash = request.getBytes(DhtConfig.KEY_INFO_HASH);
		final String infoHashHex = StringUtils.hex(infoHash);
		final TorrentSession torrentSession = TorrentManager.getInstance().torrentSession(infoHashHex);
		if(torrentSession != null) {
			final ByteBuffer buffer = ByteBuffer.allocate(6);
			final var list = PeerManager.getInstance().listPeers(infoHashHex);
			if(CollectionUtils.isNotEmpty(list)) {
				final var values = list.stream()
					.filter(peer -> peer.available())
					.filter(peer -> peer.connected())
					.limit(DhtConfig.GET_PEER_SIZE)
					.map(peer -> {
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
		response.put(DhtConfig.KEY_NODES, serializeNodes(nodes));
		return response;
	}
	
	public byte[] getInfoHash() {
		return getBytes(DhtConfig.KEY_INFO_HASH);
	}

}
