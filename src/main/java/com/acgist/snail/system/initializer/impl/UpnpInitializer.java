package com.acgist.snail.system.initializer.impl;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.upnp.UpnpClient;
import com.acgist.snail.net.upnp.UpnpServer;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.system.initializer.Initializer;

/**
 * <p>初始化UPNP</p>
 * 
 * @author acgist
 * @since 1.0.0
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
		LOGGER.info("初始化UPNP");
		UpnpServer.getInstance();
		SystemThreadContext.timer(0, 30, TimeUnit.MINUTES, () -> {
			UpnpClient.newInstance().config();
		});
	}

}
