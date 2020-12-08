package com.acgist.snail.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.BaseTest;
import com.acgist.snail.net.torrent.dht.bootstrap.NodeManager;

public class DhtConfigTest extends BaseTest {

	@Test
	public void testNodes() {
		assertNotNull(DhtConfig.getInstance().nodes());
	}
	
	@Test
	public void testPersistent() {
		final DhtConfig config = DhtConfig.getInstance();
		NodeManager.getInstance().register();
		config.persistent();
	}
	
}
