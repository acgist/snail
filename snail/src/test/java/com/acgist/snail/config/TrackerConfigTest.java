package com.acgist.snail.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.TrackerContext;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.Performance;

class TrackerConfigTest extends Performance {

	@Test
	void testPersistent() throws DownloadException {
		final TrackerConfig config = TrackerConfig.getInstance();
		TrackerContext.getInstance().sessions("https://www.acgit.com", Arrays.asList("https://www.baidu.com"));
		config.persistent();
		assertTrue(FileUtils.userDirFile("/config/bt.tracker.properties").exists());
	}
	
}
