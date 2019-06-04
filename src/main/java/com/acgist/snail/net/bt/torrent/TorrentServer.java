package com.acgist.snail.net.bt.torrent;

import com.acgist.snail.net.UdpServer;
import com.acgist.snail.system.config.SystemConfig;

/**
 * UDP服务：UTP、DHT
 * 
 * @author acgist
 * @since 1.1.0
 */
public class TorrentServer extends UdpServer<TorrentAcceptHandler> {

	private TorrentServer() {
		super(SystemConfig.getBtPort(), "Service(UTP/DHT) Server", TorrentAcceptHandler.getInstance());
		this.handler();
	}
	
	private static final TorrentServer INSTANCE = new TorrentServer();
	
	public static final TorrentServer getInstance() {
		return INSTANCE;
	}

}
