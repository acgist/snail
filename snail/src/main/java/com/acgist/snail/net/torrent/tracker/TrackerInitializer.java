package com.acgist.snail.net.torrent.tracker;

import com.acgist.snail.config.TrackerConfig;
import com.acgist.snail.context.Initializer;
import com.acgist.snail.net.DownloadException;

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
	
	@Override
	protected void destroyProxy() {
		TrackerServer.getInstance().close();
		TrackerConfig.getInstance().persistent();
	}

}
