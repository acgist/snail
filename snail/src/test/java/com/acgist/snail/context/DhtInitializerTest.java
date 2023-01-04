package com.acgist.snail.context;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.torrent.dht.DhtInitializer;
import com.acgist.snail.net.torrent.dht.NodeContext;
import com.acgist.snail.utils.Performance;

class DhtInitializerTest extends Performance {

	@Test
	void testDhtInitializer() {
		DhtInitializer.newInstance().sync();
		assertTrue(NodeContext.getInstance().nodes().size() > 0);
	}
	
}
