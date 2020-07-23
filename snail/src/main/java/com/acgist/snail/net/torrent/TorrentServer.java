package com.acgist.snail.net.torrent;

import com.acgist.snail.net.UdpServer;
import com.acgist.snail.system.config.SystemConfig;

/**
 * <p>Torrent服务端：UTP、DHT、STUN</p>
 * <p>监听端口：{@link SystemConfig#getTorrentPort()}</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class TorrentServer extends UdpServer<TorrentAcceptHandler> {
	
	private static final TorrentServer INSTANCE = new TorrentServer();
	
	public static final TorrentServer getInstance() {
		return INSTANCE;
	}

	private TorrentServer() {
		super(SystemConfig.getTorrentPort(), "Torrent(UTP/DHT/STUN) Server", TorrentAcceptHandler.getInstance());
		this.handle();
	}

}
