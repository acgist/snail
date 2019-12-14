package com.acgist.snail.system.initializer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.web.WebServer;
import com.acgist.snail.system.initializer.Initializer;

/**
 * <p>初始化WebServer</p>
 * 
 * @author acgist
 * @since 1.3.0
 */
public class WebServerInitializer extends Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebServerInitializer.class);
	
	private WebServerInitializer() {
		super(6); // 延迟启动
	}
	
	public static final WebServerInitializer newInstance() {
		return new WebServerInitializer();
	}
	
	@Override
	protected void init() throws Exception {
		LOGGER.info("初始化WebServer");
		WebServer.getInstance().launch();
	}

}
