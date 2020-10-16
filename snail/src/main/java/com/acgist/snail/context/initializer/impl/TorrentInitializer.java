package com.acgist.snail.context.initializer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.initializer.Initializer;
import com.acgist.snail.net.torrent.TorrentServer;
import com.acgist.snail.net.torrent.utp.bootstrap.UtpService;

/**
 * <p>初始化BT（DHT、UTP、STUN）服务</p>
 * 
 * @author acgist
 */
public final class TorrentInitializer extends Initializer {

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
		UtpService.getInstance().register();
	}

}
