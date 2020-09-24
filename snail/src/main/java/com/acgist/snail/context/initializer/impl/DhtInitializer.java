package com.acgist.snail.context.initializer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.initializer.Initializer;
import com.acgist.snail.net.torrent.dht.bootstrap.DhtManager;
import com.acgist.snail.net.torrent.dht.bootstrap.NodeManager;

/**
 * <p>初始化DHT</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class DhtInitializer extends Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DhtInitializer.class);
	
	private DhtInitializer() {
	}
	
	public static final DhtInitializer newInstance() {
		return new DhtInitializer();
	}
	
	@Override
	protected void init() {
		LOGGER.info("初始化DHT");
		DhtManager.getInstance().register();
		NodeManager.getInstance().register();
	}

}
