package com.acgist.snail.net.torrent.dht.bootstrap.response;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.dht.bootstrap.DhtRequest;
import com.acgist.snail.net.torrent.dht.bootstrap.DhtResponse;
import com.acgist.snail.net.torrent.dht.bootstrap.NodeManager;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerManager;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.config.DhtConfig;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>查找Peer</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class GetPeersResponse extends DhtResponse {

	private GetPeersResponse(byte[] t) {
		super(t);
		this.put(DhtConfig.KEY_TOKEN, NodeManager.getInstance().token());
	}
	
	private GetPeersResponse(DhtResponse response) {
		super(response.getT(), response.getY(), response.getR(), response.getE());
	}

	public static final GetPeersResponse newInstance(DhtResponse response) {
		return new GetPeersResponse(response);
	}

	public static final GetPeersResponse newInstance(DhtRequest request) {
		return new GetPeersResponse(request.getT());
	}
	
	/**
	 * <p>获取Token</p>
	 * 
	 * @return Token
	 */
	public byte[] getToken() {
		return getBytes(DhtConfig.KEY_TOKEN);
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

	/**
	 * <p>获取Peer列表</p>
	 * 
	 * @param request 请求
	 * 
	 * @return Peer列表
	 * 
	 * @see #getValues(DhtRequest)
	 */
	public List<PeerSession> getPeers(DhtRequest request) {
		return this.getValues(request);
	}
	
	/**
	 * <p>获取Peer列表</p>
	 * <p>自动加入系统Peer列表</p>
	 * 
	 * @param request 请求
	 * 
	 * @return Peer列表
	 */
	public List<PeerSession> getValues(DhtRequest request) {
		final byte[] infoHash = request.getBytes(DhtConfig.KEY_INFO_HASH);
		final String infoHashHex = StringUtils.hex(infoHash);
		final TorrentSession torrentSession = TorrentManager.getInstance().torrentSession(infoHashHex);
		if(torrentSession == null) {
			return List.of();
		}
		final List<?> values = this.getList(DhtConfig.KEY_VALUES);
		if(values == null) {
			return List.of();
		}
		byte[] bytes;
		PeerSession session;
		final ByteBuffer buffer = ByteBuffer.allocate(SystemConfig.IP_PORT_LENGTH);
		final List<PeerSession> list = new ArrayList<>();
		for (Object object : values) {
			bytes = (byte[]) object;
			buffer.put(bytes);
			buffer.flip();
			session = PeerManager.getInstance().newPeerSession(
				infoHashHex,
				torrentSession.statistics(),
				NetUtils.decodeIntToIp(buffer.getInt()),
				NetUtils.decodePort(buffer.getShort()),
				PeerConfig.SOURCE_DHT
			);
			buffer.flip();
			list.add(session);
		}
		return list;
	}
	
	/**
	 * <p>判断是否含有节点</p>
	 * 
	 * @return {@code true}-含有；{@code false}-不含；
	 */
	public boolean haveNodes() {
		return get(DhtConfig.KEY_NODES) != null;
	}
	
	/**
	 * <p>判断是否含有Peer</p>
	 * 
	 * @return {@code true}-含有；{@code false}-不含；
	 * 
	 * @see #haveValues()
	 */
	public boolean havePeers() {
		return haveValues();
	}
	
	/**
	 * <p>判断是否含有Peer</p>
	 * 
	 * @return {@code true}-含有；{@code false}-不含；
	 */
	public boolean haveValues() {
		return get(DhtConfig.KEY_VALUES) != null;
	}
	
}
