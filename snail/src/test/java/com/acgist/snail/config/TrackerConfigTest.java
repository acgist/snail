package com.acgist.snail.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerManager;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.Performance;

public class TrackerConfigTest extends Performance {

	@Test
	public void testPersistent() throws DownloadException {
		TrackerConfig config = TrackerConfig.getInstance();
		TrackerManager.getInstance().register();
		TrackerManager.getInstance().clients("https://www.acgit.com", Arrays.asList("https://www.baidu.com"));
		config.persistent();
		assertTrue(FileUtils.userDirFile("/config/bt.tracker.properties").exists());
	}
	
}
