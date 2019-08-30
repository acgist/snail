package com.acgist.snail.net.torrent.server;

import com.acgist.snail.net.UdpServer;
import com.acgist.snail.system.config.SystemConfig;

/**
 * Torrent服务端：UTP、DHT
 * 
 * @author acgist
 * @since 1.1.0
 */
public class TorrentServer extends UdpServer<TorrentAcceptHandler> {

	private TorrentServer() {
		super(SystemConfig.getTorrentPort(), "Torrent(UTP/DHT) Server", TorrentAcceptHandler.getInstance());
		this.handler();
	}
	
	private static final TorrentServer INSTANCE = new TorrentServer();
	
	public static final TorrentServer getInstance() {
		return INSTANCE;
	}

}
