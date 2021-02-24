package com.acgist.snail.net.torrent.utp;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class UtpRequestQueueTest {

	@Test
	void testUtpRequestQueue() {
		final UtpRequestQueue manager = UtpRequestQueue.getInstance();
		manager.requestQueue().add(UtpRequest.newInstance(null, null));
		manager.requestQueue().add(UtpRequest.newInstance(null, null));
		assertNotEquals(manager.requestQueue(), manager.requestQueue());
	}

}
