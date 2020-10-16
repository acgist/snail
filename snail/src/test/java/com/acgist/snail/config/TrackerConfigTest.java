package com.acgist.snail.config;

import org.junit.jupiter.api.Test;

import com.acgist.snail.BaseTest;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerManager;

public class TrackerConfigTest extends BaseTest {

	@Test
	public void testPersistent() throws DownloadException {
		TrackerConfig config = TrackerConfig.getInstance();
		TrackerManager.getInstance().register();
		config.persistent();
	}
	
}
