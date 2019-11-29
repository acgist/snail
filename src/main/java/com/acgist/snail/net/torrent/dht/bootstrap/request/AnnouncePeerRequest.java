package com.acgist.snail.net.torrent.dht.bootstrap.request;

import java.net.InetSocketAddress;

import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.dht.bootstrap.NodeManager;
import com.acgist.snail.net.torrent.dht.bootstrap.Request;
import com.acgist.snail.net.torrent.dht.bootstrap.Response;
import com.acgist.snail.net.torrent.dht.bootstrap.response.AnnouncePeerResponse;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerManager;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.config.DhtConfig;
import com.acgist.snail.system.config.DhtConfig.ErrorCode;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>声明Peer</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class AnnouncePeerRequest extends Request {

//	private static final Logger LOGGER = LoggerFactory.getLogger(AnnouncePeerRequest.class);
	
	private AnnouncePeerRequest() {
		super(DhtConfig.QType.ANNOUNCE_PEER);
	}
	
	/**
	 * <p>创建请求</p>
	 * 
	 * @param token token
	 * @param infoHash InfoHash
	 */
	public static final AnnouncePeerRequest newRequest(byte[] token, byte[] infoHash) {
		final AnnouncePeerRequest request = new AnnouncePeerRequest();
		request.put(DhtConfig.KEY_PORT, SystemConfig.getTorrentPortExt());
		request.put(DhtConfig.KEY_TOKEN, token);
		request.put(DhtConfig.KEY_INFO_HASH, infoHash);
		request.put(DhtConfig.KEY_IMPLIED_PORT, DhtConfig.IMPLIED_PORT_AUTO);
		return request;
	}
	
	/**
	 * <p>处理请求</p>
	 */
	public static final AnnouncePeerResponse execute(Request request) {
		final byte[] token = request.getBytes(DhtConfig.KEY_TOKEN);
		if(!ArrayUtils.equals(token, NodeManager.getInstance().token())) {
			return AnnouncePeerResponse.newInstance(Response.error(request.getT(), ErrorCode.CODE_203.code(), "Token错误"));
		}
		final byte[] infoHash = request.getBytes(DhtConfig.KEY_INFO_HASH);
		final String infoHashHex = StringUtils.hex(infoHash);
		final TorrentSession torrentSession = TorrentManager.getInstance().torrentSession(infoHashHex);
		if(torrentSession != null) {
			final Integer port = request.getInteger(DhtConfig.KEY_PORT);
			final Integer impliedPort = request.getInteger(DhtConfig.KEY_IMPLIED_PORT);
			final InetSocketAddress socketAddress = request.getSocketAddress();
			final String peerHost = socketAddress.getHostString();
			final boolean impliedPortAuto = DhtConfig.IMPLIED_PORT_AUTO.equals(impliedPort);
			Integer peerPort = port;
			if(impliedPortAuto) { // 自动配置端口
				peerPort = socketAddress.getPort();
			}
			final var peerSession = PeerManager.getInstance().newPeerSession(
				infoHashHex,
				torrentSession.statistics(),
				peerHost,
				peerPort,
				PeerConfig.SOURCE_DHT);
			if(impliedPortAuto) { // 支持UTP
				peerSession.flags(PeerConfig.PEX_UTP);
			}
		}
		return AnnouncePeerResponse.newInstance(request);
	}
	
	public Integer getPort() {
		return getInteger(DhtConfig.KEY_PORT);
	}
	
	public byte[] getToken() {
		return getBytes(DhtConfig.KEY_TOKEN);
	}
	
	public byte[] getInfoHash() {
		return getBytes(DhtConfig.KEY_INFO_HASH);
	}
	
	public Integer getImpliedPort() {
		return getInteger(DhtConfig.KEY_IMPLIED_PORT);
	}

}
