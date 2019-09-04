package com.acgist.snail.net.torrent.local;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerManager;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerService;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.pojo.wrapper.HeaderWrapper;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>本地发现消息</p>
 * <p>Local Service Discovery</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0014.html</p>
 * TODO：协议判断
 * 
 * @author acgist
 * @since 1.1.0
 */
public class LocalServiceDiscoveryMessageHandler extends UdpMessageHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LocalServiceDiscoveryMessageHandler.class);
	
	private static final String HEADER_PORT = "Port";
	private static final String HEADER_COOKIE = "cookie";
	private static final String HEADER_INFOHASH = "Infohash";

	@Override
	public void onMessage(ByteBuffer buffer, InetSocketAddress socketAddress) throws NetException {
		final String content = IoUtils.readContent(buffer);
		final String host = socketAddress.getHostString();
		doMessage(content, host);
	}

	private void doMessage(String content, String host) {
		final HeaderWrapper headers = HeaderWrapper.newInstance(content);
		final String port = headers.header(HEADER_PORT);
		final String cookie = headers.header(HEADER_COOKIE);
		final List<String> infoHashs = headers.headerList(HEADER_INFOHASH);
		if(StringUtils.isNumeric(port) && CollectionUtils.isNotEmpty(infoHashs)) {
			final byte[] peerId = StringUtils.unhex(cookie);
			if(ArrayUtils.equals(peerId, PeerService.getInstance().peerId())) { // 不是本机
				LOGGER.debug("本地发现本机忽略");
			} else {
				infoHashs.forEach(infoHash -> {
					doInfoHash(host, port, infoHash);
				});
			}
		} else {
			LOGGER.debug("不支持的本地发现消息：{}", content);
		}
	}

	private void doInfoHash(String host, String port, String infoHash) {
		final TorrentSession torrentSession = TorrentManager.getInstance().torrentSession(infoHash);
		if(torrentSession == null) {
			LOGGER.debug("本地发现，不存在的种子信息：{}", infoHash);
		} else {
			LOGGER.debug("本地发现：{}-{}-{}", infoHash, host, port);
			final PeerManager peerManager = PeerManager.getInstance();
			peerManager.newPeerSession(infoHash, torrentSession.statistics(), host, Integer.valueOf(port), PeerConfig.SOURCE_LSD);
		}
	}
	
}
