package com.acgist.snail.net.peer;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.TcpServer;
import com.acgist.snail.system.config.SystemConfig;

/**
 * Peer服务端
 */
public class PeerServer extends TcpServer {

	private static final Logger LOGGER = LoggerFactory.getLogger(PeerServer.class);
	
	/**
	 * PeerId前缀
	 */
	private static final String ID_LOGO = "AS";
	/**
	 * 20位系统ID
	 */
	public static final byte[] PEER_ID;
	/**
	 * 服务器端口
	 */
	public static final short PEER_PORT;
	
	static {
		PEER_ID = new byte[20];
		PEER_PORT = SystemConfig.getPeerPort().shortValue();
		final StringBuilder builder = new StringBuilder();
		builder.append("-").append(ID_LOGO);
		String version = SystemConfig.getVersion().replace(".", "");
		if(version.length() > 4) {
			builder.append(version.substring(0, 4));
		} else {
			builder.append("0".repeat(4 - version.length()));
			builder.append(version);
		}
		builder.append("-");
		final String peerId = builder.toString();
		System.arraycopy(peerId.getBytes(), 0, PEER_ID, 0, peerId.length());
		final Random random = new Random();
		for (int index = peerId.length(); index < 20; index++) {
			PEER_ID[index] = (byte) random.nextInt(Byte.MAX_VALUE);
		}
		LOGGER.info("系统PeerID：{}，长度：{}，端口：{}", new String(PEER_ID), PEER_ID.length, PEER_PORT);
	}

	private PeerServer() {
		super("Peer服务");
	}

	private static final PeerServer INSTANCE = new PeerServer();
	
	public static final PeerServer getInstance() {
		return INSTANCE;
	}
	
	@Override
	public boolean listen() {
		return this.listen(SystemConfig.getServerHost(), SystemConfig.getPeerPort());
	}

	@Override
	public boolean listen(String host, int port) {
		return this.listen(host, port, PeerMessageHandler.class);
	}

}
