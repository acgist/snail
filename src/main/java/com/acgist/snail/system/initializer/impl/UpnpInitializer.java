package com.acgist.snail.system.initializer.impl;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.upnp.UpnpClient;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.system.initializer.Initializer;

/**
 * UPNP初始化
 */
public class UpnpInitializer extends Initializer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UpnpInitializer.class);
	
	private UpnpInitializer() {
	}
	
	public static final UpnpInitializer newInstance() {
		return new UpnpInitializer();
	}
	
	@Override
	protected void init() {
		LOGGER.info("初始化UPNP配置");
		SystemThreadContext.timer(0, 30, TimeUnit.MINUTES, () -> {
			UpnpClient.getInstance().config();
		});
	}

}
