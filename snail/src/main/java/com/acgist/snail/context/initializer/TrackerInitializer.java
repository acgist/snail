package com.acgist.snail.context.initializer;

import com.acgist.snail.config.TrackerConfig;
import com.acgist.snail.context.TrackerContext;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.net.torrent.tracker.TrackerServer;

/**
 * Tracker初始化器
 * 
 * @author acgist
 */
public final class TrackerInitializer extends Initializer {

	private TrackerInitializer() {
		super("Tracker");
	}
	
	public static final TrackerInitializer newInstance() {
		return new TrackerInitializer();
	}
	
	@Override
	protected void init() throws DownloadException {
		TrackerConfig.getInstance();
		TrackerServer.getInstance();
		TrackerContext.getInstance();
	}

}
