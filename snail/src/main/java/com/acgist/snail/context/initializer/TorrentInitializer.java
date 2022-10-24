package com.acgist.snail.context.initializer;

import com.acgist.snail.config.PeerConfig;
import com.acgist.snail.context.UtpContext;
import com.acgist.snail.net.torrent.TorrentServer;
import com.acgist.snail.net.torrent.peer.PeerServer;

/**
 * Torrent初始化器
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
		UtpContext.getInstance();
		PeerConfig.getInstance();
		PeerServer.getInstance();
		TorrentServer.getInstance();
	}

}
