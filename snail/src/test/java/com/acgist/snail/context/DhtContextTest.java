package com.acgist.snail.context;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.torrent.dht.request.PingRequest;
import com.acgist.snail.net.torrent.dht.response.PingResponse;
import com.acgist.snail.utils.Performance;

class DhtContextTest extends Performance {

	@Test
	void testRequest() {
		final var request = PingRequest.newRequest();
		DhtContext.getInstance().request(request);
		final var response = DhtContext.getInstance().response(PingResponse.newInstance(request));
		assertNotNull(response);
	}
	
	@Test
	void testCosted() {
		this.costed(100000, () -> this.testRequest());
//		this.costed(100000, () -> DhtContext.getInstance().buildRequestId());
	}
	
}
