package com.acgist.snail.system.initializer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.lsd.LocalServiceDiscoveryServer;
import com.acgist.snail.system.initializer.Initializer;

/**
 * <p>初始化本地发现</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class LocalServiceDiscoveryInitializer extends Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(LocalServiceDiscoveryInitializer.class);
	
	private LocalServiceDiscoveryInitializer() {
	}
	
	public static final LocalServiceDiscoveryInitializer newInstance() {
		return new LocalServiceDiscoveryInitializer();
	}
	
	@Override
	protected void init() {
		LOGGER.info("初始化本地发现");
		LocalServiceDiscoveryServer.getInstance();
	}

}
