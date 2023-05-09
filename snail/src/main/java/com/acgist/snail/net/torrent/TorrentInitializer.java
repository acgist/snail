package com.acgist.snail.net.torrent;

import com.acgist.snail.config.PeerConfig;
import com.acgist.snail.context.Initializer;
import com.acgist.snail.net.torrent.peer.PeerServer;
import com.acgist.snail.net.torrent.utp.UtpContext;

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
	
	@Override
	protected void release() {
		UtpContext.getInstance().close();
		PeerServer.getInstance().close();
		TorrentServer.getInstance().close();
	}

}
