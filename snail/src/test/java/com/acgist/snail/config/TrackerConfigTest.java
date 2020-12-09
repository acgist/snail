package com.acgist.snail.config;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerManager;
import com.acgist.snail.utils.Performance;

public class TrackerConfigTest extends Performance {

	@Test
	public void testPersistent() throws DownloadException {
		TrackerConfig config = TrackerConfig.getInstance();
		TrackerManager.getInstance().register();
		config.persistent();
	}
	
}
