package com.acgist.snail.net.torrent.peer.bootstrap;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.NumberUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>Peer Service</p>
 * <p>管理Peer的ID和端口。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class PeerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PeerService.class);
	
	private static final PeerService INSTANCE = new PeerService();
	
	/**
	 * 版本信息长度
	 */
	private static final int VERSION_LENGTH = 4;
	/**
	 * PeerId前缀
	 */
	private static final String PEER_ID_PREFIX = "AS";
	
	/**
	 * 20位系统ID
	 */
	private final byte[] peerId;
	/**
	 * PeerId HTTP传输编码
	 */
	private final String peerIdUrl;

	private PeerService() {
		this.peerId = buildPeerId();
		this.peerIdUrl = buildPeerIdUrl();
	}
	
	public static final PeerService getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 生成PeerId
	 */
	private byte[] buildPeerId() {
		final byte[] peerIds = new byte[PeerConfig.PEER_ID_LENGTH];
		final StringBuilder builder = new StringBuilder(8);
		builder.append("-").append(PEER_ID_PREFIX);
		final String version = SystemConfig.getVersion().replace(".", "");
		if(version.length() > VERSION_LENGTH) {
			builder.append(version.substring(0, VERSION_LENGTH));
		} else {
			builder.append(version);
			builder.append("0".repeat(VERSION_LENGTH - version.length()));
		}
		builder.append("-");
		final String peerIdPrefix = builder.toString();
		System.arraycopy(peerIdPrefix.getBytes(), 0, peerIds, 0, peerIdPrefix.length());
		final Random random = NumberUtils.random();
		for (int index = peerIdPrefix.length(); index < PeerConfig.PEER_ID_LENGTH; index++) {
			peerIds[index] = (byte) random.nextInt(SystemConfig.UNSIGNED_BYTE_MAX);
		}
		LOGGER.info("PeerId：{}", new String(peerIds));
		return peerIds;
	}
	
	/**
	 * 生成PeerIdUrl
	 */
	private String buildPeerIdUrl() {
		int index = 0;
		final String peerIdHex = StringUtils.hex(this.peerId);
		final int length = peerIdHex.length();
		final StringBuilder builder = new StringBuilder();
		do {
			builder.append("%").append(peerIdHex.substring(index, index + 2));
			index += 2;
		} while (index < length);
		LOGGER.info("PeerIdUrl：{}", builder);
		return builder.toString();
	}
	
	public byte[] peerId() {
		return this.peerId;
	}
	
	public String peerIdUrl() {
		return this.peerIdUrl;
	}
	
}
