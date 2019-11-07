package com.acgist.snail.net.torrent;

import com.acgist.snail.net.UdpServer;
import com.acgist.snail.system.config.SystemConfig;

/**
 * Torrent服务端：UTP、DHT、STUN
 * 
 * @author acgist
 * @since 1.1.0
 */
public class TorrentServer extends UdpServer<TorrentAcceptHandler> {

	private TorrentServer() {
		super(SystemConfig.getTorrentPort(), "Torrent(UTP/DHT/STUN) Server", TorrentAcceptHandler.getInstance());
		this.handle();
	}
	
	private static final TorrentServer INSTANCE = new TorrentServer();
	
	public static final TorrentServer getInstance() {
		return INSTANCE;
	}

}
