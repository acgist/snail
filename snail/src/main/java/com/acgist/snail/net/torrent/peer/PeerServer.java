package com.acgist.snail.net.torrent.peer;

import com.acgist.snail.net.TcpServer;
import com.acgist.snail.system.config.SystemConfig;

/**
 * <p>Peer服务端</p>
 * <p>监听端口：{@link SystemConfig#getTorrentPort()}</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class PeerServer extends TcpServer<PeerMessageHandler> {
	
//	private static final Logger LOGGER = LoggerFactory.getLogger(PeerServer.class);
	
	private static final PeerServer INSTANCE = new PeerServer();
	
	public static final PeerServer getInstance() {
		return INSTANCE;
	}
	
	private PeerServer() {
		super("Peer Server", PeerMessageHandler.class);
	}
	
	@Override
	public boolean listen() {
		return this.listen(SystemConfig.getTorrentPort());
	}

}
