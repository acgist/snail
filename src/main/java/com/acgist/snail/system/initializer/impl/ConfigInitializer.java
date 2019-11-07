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
 * <p>初始化配置</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class ConfigInitializer extends Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigInitializer.class);
	
	private ConfigInitializer() {
	}
	
	public static final ConfigInitializer newInstance() {
		return new ConfigInitializer();
	}
	
	@Override
	protected void init() {
		LOGGER.info("初始化配置");
		DhtConfig.getInstance();
		SystemConfig.getInstance();
		DatabaseConfig.getInstance();
		DownloadConfig.getInstance();
		TrackerConfig.getInstance();
	}

}
