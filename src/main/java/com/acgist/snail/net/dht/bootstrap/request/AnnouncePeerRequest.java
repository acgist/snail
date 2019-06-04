package com.acgist.snail.net.dht.bootstrap.request;

import java.net.InetSocketAddress;

import com.acgist.snail.net.dht.bootstrap.DhtService;
import com.acgist.snail.net.dht.bootstrap.Request;
import com.acgist.snail.net.dht.bootstrap.Response;
import com.acgist.snail.net.dht.bootstrap.response.AnnouncePeerResponse;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.config.DhtConfig;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.manager.NodeManager;
import com.acgist.snail.system.manager.PeerManager;
import com.acgist.snail.system.manager.TorrentManager;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * 声明Peer
 * TODO：通知
 * 
 * @author acgist
 * @since 1.0.0
 */
public class AnnouncePeerRequest extends Request {

//	private static final Logger LOGGER = LoggerFactory.getLogger(AnnouncePeerRequest.class);
	
	private AnnouncePeerRequest() {
		super(DhtService.getInstance().requestId(), DhtConfig.QType.announce_peer);
		this.put(DhtConfig.KEY_ID, NodeManager.getInstance().nodeId());
	}
	
	/**
	 * 创建请求
	 * 
	 * @param token token
	 * @param infoHash infoHash
	 */
	public static final AnnouncePeerRequest newRequest(byte[] token, byte[] infoHash) {
		final AnnouncePeerRequest request = new AnnouncePeerRequest();
		request.put(DhtConfig.KEY_PORT, SystemConfig.getBtPortExt());
		request.put(DhtConfig.KEY_TOKEN, token);
		request.put(DhtConfig.KEY_INFO_HASH, infoHash);
		request.put(DhtConfig.KEY_IMPLIED_PORT, DhtConfig.IMPLIED_PORT_AUTO);
		return request;
	}
	
	/**
	 * <p>处理Peer声明</p>
	 * <p>将客户端保存到Peer列表。</p>
	 */
	public static final AnnouncePeerResponse execute(Request request) {
		final byte[] token = request.getBytes(DhtConfig.KEY_TOKEN);
		if(!ArrayUtils.equals(token, NodeManager.getInstance().nodeId())) {
			return AnnouncePeerResponse.newInstance(Response.error(request.getT(), 203, "Token错误"));
		}
		final byte[] infoHash = request.getBytes(DhtConfig.KEY_INFO_HASH);
		final String infoHashHex = StringUtils.hex(infoHash);
		final TorrentSession torrentSession = TorrentManager.getInstance().torrentSession(infoHashHex);
		if(torrentSession != null) {
			final Integer port = request.getInteger(DhtConfig.KEY_PORT);
			final Integer impliedPort = request.getInteger(DhtConfig.KEY_IMPLIED_PORT);
			final InetSocketAddress socketAddress = request.getSocketAddress();
			final String peerHost = socketAddress.getHostString();
			Integer peerPort = port;
			if(DhtConfig.IMPLIED_PORT_AUTO.equals(impliedPort)) {
				peerPort = socketAddress.getPort();
			}
			PeerManager.getInstance().newPeerSession(
				infoHashHex,
				torrentSession.taskSession().statistics(),
				peerHost,
				peerPort,
				PeerConfig.SOURCE_DHT);
		}
		return AnnouncePeerResponse.newInstance(request);
	}
	
	public Integer getPort() {
		return getInteger(DhtConfig.KEY_PORT);
	}
	
	public Integer getImpliedPort() {
		return getInteger(DhtConfig.KEY_IMPLIED_PORT);
	}
	
	public byte[] getToken() {
		return getBytes(DhtConfig.KEY_TOKEN);
	}
	
	public byte[] getInfoHash() {
		return getBytes(DhtConfig.KEY_INFO_HASH);
	}

}
