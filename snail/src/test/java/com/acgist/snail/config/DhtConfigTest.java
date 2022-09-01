package com.acgist.snail.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.NodeContext;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.Performance;

class DhtConfigTest extends Performance {

	@Test
	void testPersistent() {
		FileUtils.userDirFile("/config/bt.dht.properties").delete();
		final DhtConfig config = DhtConfig.getInstance();
		NodeContext.getInstance().newNodeSession("1".repeat(20).getBytes(), "192.168.1.1", 18888);
		config.persistent();
		assertTrue(FileUtils.userDirFile("/config/bt.dht.properties").exists());
	}
	
}
