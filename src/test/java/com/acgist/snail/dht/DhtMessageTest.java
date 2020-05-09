package com.acgist.snail.dht;

import org.junit.Test;

import com.acgist.snail.BaseTest;
import com.acgist.snail.net.torrent.dht.bootstrap.DhtResponse;

public class DhtMessageTest extends BaseTest {

	@Test
	public void testResponse() {
		final var response = DhtResponse.buildErrorResponse(new byte[] {1, 2}, 200, "test");
		this.log(response.toString());
		this.log(new String(response.toBytes()));
	}
	
}
