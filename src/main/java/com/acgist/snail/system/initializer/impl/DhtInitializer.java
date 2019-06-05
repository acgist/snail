package com.acgist.snail.system.initializer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.initializer.Initializer;
import com.acgist.snail.system.manager.NodeManager;

/**
 * <p>初始化DHT</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class DhtInitializer extends Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DhtInitializer.class);
	
	private DhtInitializer() {
	}
	
	public static final DhtInitializer newInstance() {
		return new DhtInitializer();
	}
	
	@Override
	protected void init() {
		LOGGER.info("初始DHT");
		NodeManager.getInstance().register();
	}

}
