package com.acgist.snail.system.initializer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.tracker.TrackerServer;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerManager;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.initializer.Initializer;

/**
 * <p>初始化Tracker</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TrackerInitializer extends Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DownloaderInitializer.class);
	
	private TrackerInitializer() {
	}
	
	public static final TrackerInitializer newInstance() {
		return new TrackerInitializer();
	}
	
	@Override
	protected void init() throws DownloadException {
		LOGGER.info("初始化Tracker");
		TrackerServer.getInstance();
		TrackerManager.getInstance().register();
	}

}
