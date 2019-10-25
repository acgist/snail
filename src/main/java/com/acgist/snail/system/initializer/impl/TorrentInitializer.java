package com.acgist.snail.system.initializer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.server.TorrentServer;
import com.acgist.snail.system.initializer.Initializer;

/**
 * <p>初始化BT（DHT、UTP、STUN）服务</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TorrentInitializer extends Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentInitializer.class);
	
	private TorrentInitializer() {
	}
	
	public static final TorrentInitializer newInstance() {
		return new TorrentInitializer();
	}
	
	@Override
	protected void init() {
		LOGGER.info("初始化BT（DHT、UTP、STUN）服务");
		TorrentServer.getInstance();
	}

}
