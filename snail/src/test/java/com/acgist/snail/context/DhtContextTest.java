package com.acgist.snail.context;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.torrent.dht.DhtContext;
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

	@Test
	void testRequestId() {
		this.log(DhtContext.getInstance().buildRequestId());
		this.log(DhtContext.getInstance().buildRequestId());
		this.costed(100000, () -> {
			assertFalse(Arrays.equals(DhtContext.getInstance().buildRequestId(), DhtContext.getInstance().buildRequestId()));
		});
	}
	
}
