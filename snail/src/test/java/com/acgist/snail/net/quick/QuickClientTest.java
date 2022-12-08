package com.acgist.snail.net.quick;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.NetException;

class QuickClientTest {

	@Test
	void testTransport() throws NetException {
		final QuickClient client = new QuickClient();
		client.quick("host:localhost:18888", new File("D:/tmp/snail/graalvm-ce-java17-windows-amd64-22.3.0.zip"));
		client.close();
	}
	
}
