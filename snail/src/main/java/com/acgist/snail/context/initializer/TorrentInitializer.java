package com.acgist.snail.context.initializer;

import com.acgist.snail.config.PeerConfig;
import com.acgist.snail.net.torrent.TorrentServer;
import com.acgist.snail.net.torrent.peer.PeerServer;
import com.acgist.snail.net.torrent.utp.UtpService;

/**
 * <p>Torrent初始化器</p>
 * 
 * @author acgist
 */
public final class TorrentInitializer extends Initializer {

	private TorrentInitializer() {
		super("Torrent");
	}
	
	public static final TorrentInitializer newInstance() {
		return new TorrentInitializer();
	}
	
	@Override
	protected void init() {
		PeerConfig.getInstance();
		TorrentServer.getInstance();
		PeerServer.getInstance();
		UtpService.getInstance();
	}

}
