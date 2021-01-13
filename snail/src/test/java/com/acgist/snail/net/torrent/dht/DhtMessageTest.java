package com.acgist.snail.net.torrent.dht;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.torrent.dht.request.PingRequest;
import com.acgist.snail.net.torrent.dht.response.PingResponse;
import com.acgist.snail.utils.Performance;

public class DhtMessageTest extends Performance {

	@Test
	public void testDhtMessage() {
		final var request = PingRequest.newRequest();
		this.log(request.toString());
		this.log(new String(request.toBytes()));
		assertNotNull(request);
		final var response = DhtResponse.buildErrorResponse(new byte[] {1, 2}, 200, "错误信息");
		this.log(response.toString());
		this.log(new String(response.toBytes()));
		assertNotNull(response);
	}

	@Test
	public void testErrorResponse() {
		int code = 200;
		String message = "acgist";
		var response = DhtResponse.buildErrorResponse("1".repeat(20).getBytes(), 200, "acgist");
		assertEquals(code, response.errorCode());
		assertEquals(message, response.errorMessage());
		response = PingResponse.newInstance(response);
		assertEquals(code, response.errorCode());
		assertEquals(message, response.errorMessage());
		response = PingResponse.newInstance(PingRequest.newRequest());
		assertEquals(201, response.errorCode());
		assertEquals("未知错误", response.errorMessage());
	}

	@Test
	public void testEquals() {
		final PingRequest request = PingRequest.newRequest();
		final Object response = PingResponse.newInstance(request);
		assertFalse(request.equals(response));
	}
	
}
