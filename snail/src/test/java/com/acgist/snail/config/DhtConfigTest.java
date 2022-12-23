package com.acgist.snail.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.torrent.dht.NodeContext;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.MapUtils;
import com.acgist.snail.utils.Performance;

class DhtConfigTest extends Performance {

	@Test
	void testPersistent() {
		FileUtils.userDirFile(DhtConfig.DHT_CONFIG).delete();
		final DhtConfig config = DhtConfig.getInstance();
		assertTrue(MapUtils.isNotEmpty(config.nodes()));
		NodeContext.getInstance().newNodeSession("1".repeat(20).getBytes(), "192.168.1.1", 18888);
		config.persistent();
		assertTrue(FileUtils.userDirFile(DhtConfig.DHT_CONFIG).exists());
	}
	
}
