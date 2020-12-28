package com.acgist.snail.net.torrent.dht;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.torrent.dht.request.PingRequest;
import com.acgist.snail.net.torrent.dht.response.PingResponse;
import com.acgist.snail.utils.Performance;

public class DhtManagerTest extends Performance {

	@Test
	public void testRequest() {
		final var request = PingRequest.newRequest();
		DhtManager.getInstance().request(request);
		final var response = DhtManager.getInstance().response(PingResponse.newInstance(request));
		assertNotNull(response);
	}
	
}
