package com.acgist.snail.config;

import org.junit.jupiter.api.Test;

import com.acgist.snail.BaseTest;
import com.acgist.snail.net.torrent.dht.bootstrap.NodeManager;

public class DhtConfigTest extends BaseTest {

	@Test
	public void testPersistent() {
		DhtConfig config = DhtConfig.getInstance();
		NodeManager.getInstance().register();
		config.persistent();
	}
	
}
