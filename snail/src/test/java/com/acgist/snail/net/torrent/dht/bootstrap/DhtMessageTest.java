package com.acgist.snail.net.torrent.dht.bootstrap;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

public class DhtMessageTest extends Performance {

	@Test
	public void testResponse() {
		final var response = DhtResponse.buildErrorResponse(new byte[] {1, 2}, 200, "test");
		this.log(response.toString());
		this.log(new String(response.toBytes()));
	}
	
}
