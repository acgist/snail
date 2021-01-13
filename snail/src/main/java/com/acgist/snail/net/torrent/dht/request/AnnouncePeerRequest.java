package com.acgist.snail.net.torrent.dht.request;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.DhtConfig;
import com.acgist.snail.config.DhtConfig.ErrorCode;
import com.acgist.snail.config.PeerConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.dht.DhtManager;
import com.acgist.snail.net.torrent.dht.DhtRequest;
import com.acgist.snail.net.torrent.dht.DhtResponse;
import com.acgist.snail.net.torrent.dht.response.AnnouncePeerResponse;
import com.acgist.snail.net.torrent.peer.PeerManager;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>声明Peer</p>
 * <p>声明当前节点作为Peer进行下载和上传</p>
 * 
 * @author acgist
 */
public final class AnnouncePeerRequest extends DhtRequest {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AnnouncePeerRequest.class);

	private AnnouncePeerRequest() {
		super(DhtConfig.QType.ANNOUNCE_PEER);
	}
	
	/**
	 * <p>创建请求</p>
	 * 
	 * @param token token
	 * @param infoHash InfoHash
	 * 
	 * @return 请求
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
	 * 
	 * @param request 请求
	 * 
	 * @return 响应
	 */
	public static final AnnouncePeerResponse execute(DhtRequest request) {
		final byte[] token = request.getBytes(DhtConfig.KEY_TOKEN);
		// 验证Token
		if(!ArrayUtils.equals(token, DhtManager.getInstance().token())) {
			return AnnouncePeerResponse.newInstance(DhtResponse.buildErrorResponse(request.getT(), ErrorCode.CODE_203.code(), "Token错误"));
		}
		final byte[] infoHash = request.getBytes(DhtConfig.KEY_INFO_HASH);
		final String infoHashHex = StringUtils.hex(infoHash);
		final TorrentSession torrentSession = TorrentManager.getInstance().torrentSession(infoHashHex);
		if(torrentSession != null) {
			// 默认端口
			Integer peerPort = request.getInteger(DhtConfig.KEY_PORT);
			final Integer impliedPort = request.getInteger(DhtConfig.KEY_IMPLIED_PORT);
			final InetSocketAddress socketAddress = request.getSocketAddress();
			final String peerHost = socketAddress.getHostString();
			// 是否自动配置端口
			final boolean impliedPortAuto = DhtConfig.IMPLIED_PORT_AUTO.equals(impliedPort);
			if(impliedPortAuto) {
				// 自动配置端口
				peerPort = socketAddress.getPort();
			}
			final var peerSession = PeerManager.getInstance().newPeerSession(
				infoHashHex,
				torrentSession.statistics(),
				peerHost,
				peerPort,
				PeerConfig.Source.DHT
			);
			if(impliedPortAuto) {
				// 支持UTP
				peerSession.flags(PeerConfig.PEX_UTP);
			}
		} else {
			LOGGER.debug("声明Peer种子信息不存在：{}", infoHashHex);
		}
		return AnnouncePeerResponse.newInstance(request);
	}
	
	/**
	 * <p>获取端口</p>
	 * 
	 * @return 端口
	 */
	public Integer getPort() {
		return this.getInteger(DhtConfig.KEY_PORT);
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
	 * <p>获取InfoHash</p>
	 * 
	 * @return InfoHash
	 */
	public byte[] getInfoHash() {
		return this.getBytes(DhtConfig.KEY_INFO_HASH);
	}
	
	/**
	 * <p>获取ImpliedPort</p>
	 * 
	 * @return ImpliedPort
	 */
	public Integer getImpliedPort() {
		return this.getInteger(DhtConfig.KEY_IMPLIED_PORT);
	}

}
