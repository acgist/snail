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
import com.acgist.snail.system.manager.PeerSessionManager;
import com.acgist.snail.system.manager.TorrentSessionManager;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.StringUtils;

public class AnnouncePeerRequest extends Request {

	private AnnouncePeerRequest() {
		super(DhtService.getInstance().id(), DhtConfig.QType.announce_peer);
		this.put(DhtConfig.KEY_ID, NodeManager.getInstance().nodeId());
	}
	
	public static final AnnouncePeerRequest newRequest(byte[] token, byte[] infoHash) {
		final AnnouncePeerRequest request = new AnnouncePeerRequest();
		request.put(DhtConfig.KEY_PORT, SystemConfig.getPeerPort());
		request.put(DhtConfig.KEY_TOKEN, token);
		request.put(DhtConfig.KEY_INFO_HASH, infoHash);
		request.put(DhtConfig.KEY_IMPLIED_PORT, DhtConfig.IMPLIED_PORT_CONFIG); // TODO：实现uTP，修改：1
		return request;
	}
	
	public static final AnnouncePeerResponse execute(Request request) {
		final byte[] token = request.getBytes(DhtConfig.KEY_TOKEN);
		if(!ArrayUtils.equals(token, NodeManager.getInstance().nodeId())) {
			return AnnouncePeerResponse.newInstance(Response.error(request.getT(), 201, "Token错误"));
		}
		final byte[] infoHash = request.getBytes(DhtConfig.KEY_INFO_HASH);
		final String infoHashHex = StringUtils.hex(infoHash);
		final TorrentSession torrentSession = TorrentSessionManager.getInstance().torrentSession(infoHashHex);
		if(torrentSession != null) {
			final Integer port = request.getInteger(DhtConfig.KEY_PORT);
			final Integer impliedPort = request.getInteger(DhtConfig.KEY_IMPLIED_PORT);
			final InetSocketAddress address = request.getAddress();
			String peerHost = address.getHostString();
			Integer peerPort = port;
			if(DhtConfig.IMPLIED_PORT_AUTO.equals(impliedPort)) {
				peerPort = address.getPort();
			}
			PeerSessionManager.getInstance().newPeerSession(
				infoHashHex,
				torrentSession.taskSession().statistics(),
				peerHost,
				peerPort,
				PeerConfig.SOURCE_DHT);
		}
		final AnnouncePeerResponse response = AnnouncePeerResponse.newInstance(request);
		return response;
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
