package com.acgist.snail.initializer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.initializer.Initializer;
import com.acgist.snail.net.torrent.lsd.LocalServiceDiscoveryServer;

/**
 * <p>初始化本地发现</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class LocalServiceDiscoveryInitializer extends Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(LocalServiceDiscoveryInitializer.class);

	/**
	 * <p>启动延长时间：{@value}</p>
	 */
	private static final int DELAY = 6;
	
	private LocalServiceDiscoveryInitializer() {
		super(DELAY); // 延迟启动
	}
	
	public static final LocalServiceDiscoveryInitializer newInstance() {
		return new LocalServiceDiscoveryInitializer();
	}
	
	@Override
	protected void init() {
		LOGGER.info("初始化本地发现");
		LocalServiceDiscoveryServer.getInstance().register();
	}

}
