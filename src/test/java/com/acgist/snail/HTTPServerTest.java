package com.acgist.snail;

import org.junit.Test;

import com.acgist.snail.player.web.WebServer;

public class HTTPServerTest extends BaseTest {

	@Test
	public void testServer() throws Exception {
		WebServer.getInstance().launch();
		this.pause();
	}
	
}
