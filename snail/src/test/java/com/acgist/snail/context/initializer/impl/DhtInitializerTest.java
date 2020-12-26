package com.acgist.snail.context.initializer.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.torrent.dht.DhtManager;
import com.acgist.snail.net.torrent.dht.NodeManager;
import com.acgist.snail.net.torrent.dht.request.PingRequest;
import com.acgist.snail.net.torrent.dht.response.PingResponse;
import com.acgist.snail.utils.Performance;

public class DhtInitializerTest extends Performance {

	@Test
	public void testDhtInitializer() {
		DhtInitializer.newInstance().sync();
		assertTrue(NodeManager.getInstance().nodes().size() > 0);
		final var request = PingRequest.newRequest();
		DhtManager.getInstance().request(request);
		final var response = DhtManager.getInstance().response(PingResponse.newInstance(request));
		assertNotNull(response);
	}
	
}
