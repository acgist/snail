package com.acgist.snail.context.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.PeerConfig;
import com.acgist.snail.net.torrent.TorrentServer;
import com.acgist.snail.net.torrent.peer.PeerServer;
import com.acgist.snail.net.torrent.utp.UtpService;

/**
 * <p>初始化BT（DHT、UTP、STUN）服务</p>
 * 
 * @author acgist
 */
public final class TorrentInitializer extends Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentInitializer.class);
	
	/**
	 * <p>禁止创建实例</p>
	 */
	private TorrentInitializer() {
	}
	
	public static final TorrentInitializer newInstance() {
		return new TorrentInitializer();
	}
	
	@Override
	protected void init() {
		LOGGER.debug("初始化BT（DHT、UTP、STUN）服务");
		PeerConfig.getInstance();
		TorrentServer.getInstance();
		PeerServer.getInstance();
		UtpService.getInstance();
	}

}
