package com.acgist.snail.net.peer;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.TcpServer;
import com.acgist.snail.system.config.SystemConfig;

public class PeerServer extends TcpServer {

	protected PeerServer() {
		super("Peer服务");
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(PeerServer.class);
	
	/**
	 * PeerId前缀
	 */
	private static final String ID_SUFFIX = "-AS8888-";
	/**
	 * 20位系统ID
	 */
	public static final String PEER_ID;
	/**
	 * 服务器端口
	 */
	public static final short PORT = SystemConfig.getPeerPort().shortValue();
	
	static {
		final Random random = new Random();
		final StringBuilder builder = new StringBuilder(ID_SUFFIX);
		final int length = 20 - ID_SUFFIX.length();
		for (int index = 0; index < length; index++) {
			builder.append(random.nextInt(10));
		}
		PEER_ID = builder.toString();
		LOGGER.info("系统PeerID：{}，长度：{}", PEER_ID, PEER_ID.length());
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
