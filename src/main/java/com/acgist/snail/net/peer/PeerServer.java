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
	public static final String PEER_ID;
	/**
	 * 服务器端口
	 */
	public static final short PEER_PORT;
	
	static {
		final Random random = new Random();
		final StringBuilder peerIdBuilder = new StringBuilder();
		peerIdBuilder.append("-").append(ID_LOGO);
		String version = SystemConfig.getVersion().replace(".", "");
		if(version.length() > 4) {
			peerIdBuilder.append(version.substring(0, 4));
		} else {
			peerIdBuilder.append("0".repeat(4 - version.length()));
			peerIdBuilder.append(version);
		}
		peerIdBuilder.append("-");
		final int length = 20 - peerIdBuilder.length();
		for (int index = 0; index < length; index++) {
			peerIdBuilder.append(random.nextInt(10));
		}
		PEER_ID = peerIdBuilder.toString();
		PEER_PORT = SystemConfig.getPeerPort().shortValue();
		LOGGER.info("系统PeerID：{}，长度：{}，端口：{}", PEER_ID, PEER_ID.length(), PEER_PORT);
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
