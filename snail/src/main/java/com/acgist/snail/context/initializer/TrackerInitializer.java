package com.acgist.snail.context.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.TrackerConfig;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.net.torrent.tracker.TrackerManager;
import com.acgist.snail.net.torrent.tracker.TrackerServer;

/**
 * <p>初始化Tracker</p>
 * 
 * @author acgist
 */
public final class TrackerInitializer extends Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerInitializer.class);
	
	/**
	 * <p>禁止创建实例</p>
	 */
	private TrackerInitializer() {
	}
	
	public static final TrackerInitializer newInstance() {
		return new TrackerInitializer();
	}
	
	@Override
	protected void init() throws DownloadException {
		LOGGER.debug("初始化Tracker");
		TrackerConfig.getInstance();
		TrackerServer.getInstance();
		TrackerManager.getInstance();
	}

}
