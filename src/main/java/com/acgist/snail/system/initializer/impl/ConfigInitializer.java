package com.acgist.snail.system.initializer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.config.DatabaseConfig;
import com.acgist.snail.system.config.DhtConfig;
import com.acgist.snail.system.config.DownloadConfig;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.config.TrackerConfig;
import com.acgist.snail.system.initializer.Initializer;

/**
 * 初始化：配置
 */
public class ConfigInitializer extends Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigInitializer.class);
	
	private ConfigInitializer() {
	}
	
	public static final ConfigInitializer newInstance() {
		return new ConfigInitializer();
	}
	
	@Override
	protected void init() {
		LOGGER.info("初始化配置信息");
		SystemConfig.getInstance();
		DatabaseConfig.getInstance();
		DownloadConfig.getInstance();
		DhtConfig.getInstance();
		TrackerConfig.getInstance();
	}

}
