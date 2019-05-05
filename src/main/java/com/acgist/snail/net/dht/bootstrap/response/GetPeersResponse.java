package com.acgist.snail.net.dht.bootstrap.response;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.acgist.snail.net.dht.bootstrap.Request;
import com.acgist.snail.net.dht.bootstrap.Response;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.config.DhtConfig;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.manager.NodeManager;
import com.acgist.snail.system.manager.PeerSessionManager;
import com.acgist.snail.system.manager.TorrentSessionManager;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.StringUtils;

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

	public static final FindNodeResponse newInstance(Request request) {
		final FindNodeResponse response = new FindNodeResponse(request.getT());
		final byte[] infoHash = request.getBytes(DhtConfig.KEY_INFO_HASH);
		final String infoHashHex = StringUtils.hex(infoHash);
		TorrentSession session = TorrentSessionManager.getInstance().torrentSession(infoHashHex);
		response.put(DhtConfig.KEY_TOKEN, NodeManager.getInstance().token());
		final ByteBuffer buffer = ByteBuffer.allocate(6);
		if(session != null) {
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
		
	public byte[] getToken() {
		return getBytes(DhtConfig.KEY_TOKEN);
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
	
	public List<PeerSession> putPeers(Request request) {
		return this.getValues(request);
	}
	
	public List<PeerSession> getValues(Request request) {
		final byte[] infoHash = request.getBytes(DhtConfig.KEY_INFO_HASH);
		final String infoHashHex = StringUtils.hex(infoHash);
		final TorrentSession torrentSession = TorrentSessionManager.getInstance().torrentSession(infoHashHex);
		if(torrentSession == null) {
			return null;
		}
		final List<?> values = this.getList(DhtConfig.KEY_VALUES);
		if(values == null) {
			return null;
		}
		byte[] bytes;
		ByteBuffer buffer;
		PeerSession session;
		final List<PeerSession> list = new ArrayList<>();
		for (Object object : values) {
			bytes = (byte[]) object;
			buffer = ByteBuffer.wrap(bytes);
			session = PeerSessionManager.getInstance().newPeerSession(infoHashHex, null, NetUtils.decodeIntToIp(buffer.getInt()), NetUtils.decodePort(buffer.getShort()), PeerConfig.SOURCE_DHT);
			list.add(session);
		}
		return list;
	}
	
	public boolean havePeers() {
		return get(DhtConfig.KEY_VALUES) != null;
	}
	
	public boolean haveNodes() {
		return get(DhtConfig.KEY_NODES) != null;
	}
	
}
