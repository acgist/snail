package com.acgist.snail.net.bt.peer.bootstrap;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.config.SystemConfig;

/**
 * <p>Peer Service</p>
 * <p>管理Peer的ID和端口。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class PeerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PeerService.class);
	
	private static final PeerService INSTANCE = new PeerService();
	
	/**
	 * PeerId前缀
	 */
	private static final String PEER_ID_PREFIX = "AS";
	/**
	 * 20位系统ID
	 */
	private final byte[] peerId;

	private PeerService() {
		this.peerId = buildPeerId();
	}
	
	public static final PeerService getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 生成PeerId
	 */
	private byte[] buildPeerId() {
		final byte[] peerId = new byte[PeerConfig.PEER_ID_LENGTH];
		final StringBuilder builder = new StringBuilder(8);
		builder.append("-").append(PEER_ID_PREFIX);
		final String version = SystemConfig.getVersion().replace(".", "");
		if(version.length() > 4) {
			builder.append(version.substring(0, 4));
		} else {
			builder.append(version);
			builder.append("0".repeat(4 - version.length()));
		}
		builder.append("-");
		final String peerIdPrefix = builder.toString();
		System.arraycopy(peerIdPrefix.getBytes(), 0, peerId, 0, peerIdPrefix.length());
		final Random random = new Random();
		for (int index = peerIdPrefix.length(); index < PeerConfig.PEER_ID_LENGTH; index++) {
			peerId[index] = (byte) random.nextInt(SystemConfig.UNSIGNED_BYTE_SIZE);
		}
		LOGGER.info("PeerId：{}", new String(peerId));
		return peerId;
	}
	
	public byte[] peerId() {
		return this.peerId;
	}

}
