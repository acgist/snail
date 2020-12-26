package com.acgist.snail.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.torrent.dht.NodeManager;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.Performance;

public class DhtConfigTest extends Performance {

	@Test
	public void testNodes() {
		assertNotNull(DhtConfig.getInstance().nodes());
	}
	
	@Test
	public void testPersistent() {
		final DhtConfig config = DhtConfig.getInstance();
		NodeManager.getInstance().newNodeSession("1".repeat(20).getBytes(), "192.168.1.1", 2020);
		config.persistent();
		assertTrue(FileUtils.userDirFile("/config/bt.dht.properties").exists());
	}
	
}
