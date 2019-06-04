package com.acgist.snail.system.initializer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.bt.torrent.TorrentServer;
import com.acgist.snail.system.initializer.Initializer;

/**
 * 初始化：启动DHT和UTP服务
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
		LOGGER.info("初始化DHT、UDP服务");
		TorrentServer.getInstance();
	}

}
