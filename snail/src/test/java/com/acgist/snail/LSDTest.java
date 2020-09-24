package com.acgist.snail;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.net.torrent.lsd.LocalServiceDiscoveryClient;
import com.acgist.snail.net.torrent.lsd.LocalServiceDiscoveryServer;

public class LSDTest extends BaseTest {

	@Test
	public void testServer() {
		LocalServiceDiscoveryServer.getInstance();
		this.pause();
	}
	
	@Test
	public void testClient() throws NetException {
		var client = LocalServiceDiscoveryClient.newInstance();
		client.localSearch(
			"28b5e72737f183cb36182fcc8991d5cbf7ce627c",
			"28b5e72737f183cb36182fcc8991d5cbf7ce6271"
		);
		client.localSearch("28b5e72737f183cb36182fcc8991d5cbf7ce6272");
	}
	
}
