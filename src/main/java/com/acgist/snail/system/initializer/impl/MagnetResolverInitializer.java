package com.acgist.snail.system.initializer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.protocol.magnet.impl.BtbttvMagnetResolver;
import com.acgist.snail.system.initializer.AInitializer;
import com.acgist.snail.system.manager.MagnetResolverManager;

/**
 * 初始化：磁力链接转换器
 */
public class MagnetResolverInitializer extends AInitializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(MagnetResolverInitializer.class);
	
	@Override
	protected void init() {
		LOGGER.info("初始化磁力链接转换器");
		var manager = MagnetResolverManager.getInstance();
		manager.register(BtbttvMagnetResolver.newInstance());
		manager.sort();
	}
	
}
