package com.acgist.snail.system.initializer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.bt.tracker.TrackerServer;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.initializer.Initializer;
import com.acgist.snail.system.manager.TrackerManager;

/**
 * 初始化：注册默认Tracker Client。
 */
public class TrackerInitializer extends Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DownloaderInitializer.class);
	
	private TrackerInitializer() {
	}
	
	public static final TrackerInitializer newInstance() {
		return new TrackerInitializer();
	}
	
	@Override
	protected void init() {
		LOGGER.info("注册默认Tracker Client");
		try {
			TrackerManager.getInstance().register();
		} catch (DownloadException e) {
			LOGGER.error("注册默认Tracker Client异常", e);
		}
		LOGGER.info("初始化Tracker Server");
		TrackerServer.getInstance();
	}

}
