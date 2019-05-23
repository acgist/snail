package com.acgist.snail.system.initializer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.bootstrap.UdpServiceServer;
import com.acgist.snail.system.initializer.Initializer;

/**
 * 初始化：启动DHT和UTP服务
 */
public class UdpServiceInitializer extends Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(UdpServiceInitializer.class);
	
	private UdpServiceInitializer() {
	}
	
	public static final UdpServiceInitializer newInstance() {
		return new UdpServiceInitializer();
	}
	
	@Override
	protected void init() {
		LOGGER.info("初始化DHT、UDP服务");
		UdpServiceServer.getInstance();
	}

}
