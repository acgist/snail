package com.acgist.snail.module.initializer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.module.initializer.Initializer;

/**
 * 配置初始化
 */
public class ConfigInitializer extends Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigInitializer.class);
	
	@Override
	protected void init() {
		LOGGER.info("初始化配置信息");
	}

}
