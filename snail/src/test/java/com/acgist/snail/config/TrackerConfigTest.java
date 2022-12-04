package com.acgist.snail.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.DownloadException;
import com.acgist.snail.net.torrent.tracker.TrackerContext;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.Performance;

class TrackerConfigTest extends Performance {

	@Test
	void testAnnounces() {
		assertNotNull(TrackerConfig.getInstance().announces());
	}
	
	@Test
	void testPersistent() throws DownloadException {
		FileUtils.userDirFile("/config/bt.tracker.properties").delete();
		final TrackerConfig config = TrackerConfig.getInstance();
		TrackerContext.getInstance().sessions("https://www.acgit.com", Arrays.asList("https://www.baidu.com"));
		config.persistent();
		assertTrue(FileUtils.userDirFile("/config/bt.tracker.properties").exists());
	}
	
}
