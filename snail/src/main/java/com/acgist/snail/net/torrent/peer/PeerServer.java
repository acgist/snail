package com.acgist.snail.net.torrent.peer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.net.TcpServer;

/**
 * <p>Peer服务端</p>
 * <p>监听端口：{@link SystemConfig#getTorrentPort()}</p>
 * 
 * @author acgist
 */
public final class PeerServer extends TcpServer<PeerMessageHandler> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PeerServer.class);
	
	private static final PeerServer INSTANCE = new PeerServer();
	
	public static final PeerServer getInstance() {
		return INSTANCE;
	}
	
	private PeerServer() {
		super("Peer Server", PeerMessageHandler.class);
		this.register();
	}
	
	@Override
	public boolean listen() {
		return this.listen(SystemConfig.getTorrentPort());
	}

	/**
	 * <p>注册Peer服务监听</p>
	 * 
	 * @see #listen()
	 */
	private void register() {
		LOGGER.debug("注册Peer服务监听");
		this.listen();
	}

}
