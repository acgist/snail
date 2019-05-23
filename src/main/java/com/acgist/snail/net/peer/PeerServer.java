package com.acgist.snail.net.peer;

import com.acgist.snail.net.TcpServer;
import com.acgist.snail.system.config.SystemConfig;

/**
 * Peer服务端
 * 
 * @author acgist
 * @since 1.0.0
 */
public class PeerServer extends TcpServer<PeerMessageHandler> {

//	private static final Logger LOGGER = LoggerFactory.getLogger(PeerServer.class);
	
	private PeerServer() {
		super("Peer Server", PeerMessageHandler.class);
	}

	private static final PeerServer INSTANCE = new PeerServer();
	
	public static final PeerServer getInstance() {
		return INSTANCE;
	}
	
	@Override
	public boolean listen() {
		return this.listen(SystemConfig.getServicePort());
	}

}
