package com.acgist.snail.tcp;

import org.junit.Test;

import com.acgist.snail.net.TcpServer;
import com.acgist.snail.utils.ThreadUtils;

public class TcpServerTest {

	@Test
	public void server() throws Exception {
		TcpServer<TcpMessage> server = new TcpServer<TcpMessage>("TEST", TcpMessage.class) {
			@Override
			public boolean listen() {
				return listen(4567);
			}
		};
		server.listen();
		ThreadUtils.sleep(Long.MAX_VALUE);
	}
	
}
