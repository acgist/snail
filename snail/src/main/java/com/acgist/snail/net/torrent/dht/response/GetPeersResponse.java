package com.acgist.snail.net.torrent.dht.response;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.acgist.snail.config.DhtConfig;
import com.acgist.snail.config.PeerConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.net.torrent.TorrentContext;
import com.acgist.snail.net.torrent.TorrentSession;
import com.acgist.snail.net.torrent.dht.DhtContext;
import com.acgist.snail.net.torrent.dht.DhtRequest;
import com.acgist.snail.net.torrent.dht.DhtResponse;
import com.acgist.snail.net.torrent.dht.NodeSession;
import com.acgist.snail.net.torrent.peer.PeerContext;
import com.acgist.snail.net.torrent.peer.PeerSession;
import com.acgist.snail.utils.NetUtils;

/**
 * <p>查找Peer</p>
 * 
 * @author acgist
 */
public final class GetPeersResponse extends DhtResponse {

	/**
	 * @param t 节点ID
	 */
	private GetPeersResponse(byte[] t) {
		super(t);
		this.put(DhtConfig.KEY_TOKEN, DhtContext.getInstance().token());
	}
	
	/**
	 * @param response 响应
	 */
	private GetPeersResponse(DhtResponse response) {
		super(response.getT(), response.getY(), response.getR(), response.getE());
	}
	
	/**
	 * <p>新建响应</p>
	 * 
	 * @param request 请求
	 * 
	 * @return 响应
	 */
	public static final GetPeersResponse newInstance(DhtRequest request) {
		return new GetPeersResponse(request.getT());
	}

	/**
	 * <p>新建响应</p>
	 * 
	 * @param response 响应
	 * 
	 * @return 响应
	 */
	public static final GetPeersResponse newInstance(DhtResponse response) {
		return new GetPeersResponse(response);
	}
	
	/**
	 * <p>获取Token</p>
	 * 
	 * @return Token
	 */
	public byte[] getToken() {
		return this.getBytes(DhtConfig.KEY_TOKEN);
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

	/**
	 * <p>获取Peer列表</p>
	 * 
	 * @param infoHashHex InfoHash Hex
	 * 
	 * @return Peer列表
	 * 
	 * @see #getValues(String)
	 */
	public List<PeerSession> getPeers(String infoHashHex) {
		return this.getValues(infoHashHex);
	}
	
	/**
	 * <p>获取Peer列表</p>
	 * 
	 * @param infoHashHex InfoHash Hex
	 * 
	 * @return Peer列表
	 */
	public List<PeerSession> getValues(String infoHashHex) {
		final TorrentSession torrentSession = TorrentContext.getInstance().torrentSession(infoHashHex);
		if(torrentSession == null) {
			return List.of();
		}
		final List<?> values = this.getList(DhtConfig.KEY_VALUES);
		if(values == null) {
			return List.of();
		}
		PeerSession session;
		// TODO：IPv6
		final ByteBuffer buffer = ByteBuffer.allocate(SystemConfig.IPV4_PORT_LENGTH);
		final List<PeerSession> list = new ArrayList<>();
		for (Object object : values) {
			buffer.put((byte[]) object);
			buffer.flip();
			session = PeerContext.getInstance().newPeerSession(
				infoHashHex,
				torrentSession.statistics(),
				NetUtils.intToIP(buffer.getInt()),
				NetUtils.portToInt(buffer.getShort()),
				PeerConfig.Source.DHT
			);
			buffer.flip();
			list.add(session);
		}
		return list;
	}
	
	/**
	 * <p>判断是否含有节点</p>
	 * 
	 * @return 是否含有节点
	 */
	public boolean hasNodes() {
		// TODO：want
		return this.get(DhtConfig.KEY_NODES) != null;
	}
	
	/**
	 * <p>判断是否含有Peer</p>
	 * 
	 * @return 是否含有Peer
	 * 
	 * @see #hasValues()
	 */
	public boolean hasPeers() {
		return this.hasValues();
	}
	
	/**
	 * <p>判断是否含有Peer</p>
	 * 
	 * @return 是否含有Peer
	 */
	public boolean hasValues() {
		return this.get(DhtConfig.KEY_VALUES) != null;
	}

}
