package com.acgist.snail.context.initializer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.initializer.Initializer;
import com.acgist.snail.net.torrent.lsd.LocalServiceDiscoveryServer;

/**
 * <p>初始化本地发现</p>
 * 
 * @author acgist
 */
public final class LocalServiceDiscoveryInitializer extends Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(LocalServiceDiscoveryInitializer.class);

	/**
	 * <p>启动延迟时间：{@value}</p>
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
