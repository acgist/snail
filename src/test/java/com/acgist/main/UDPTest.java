package com.acgist.main;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.junit.Test;

import com.acgist.snail.net.UdpClient;
import com.acgist.snail.net.UdpServer;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.ThreadUtils;

public class UDPTest {

	@Test
	public void server() {
		UdpServer server = new UdpServer("TestServer") {
			@Override
			public boolean listen(String host, int port) {
				return listen(host, port, UdpTestMessageHandler.class);
			}
			@Override
			public boolean listen() {
				return listen("127.0.0.1", 18888);
			}
		};
		server.listen();
		ThreadUtils.sleep(1000000);
	}

	@Test
	public void client() {
		UdpClient<UdpTestMessageHandler> client = new UdpClient<UdpTestMessageHandler>("TestClient", new UdpTestMessageHandler()) {
		};
		client.open();
		client.handle();
		while (true) {
			try {
				final String message = System.currentTimeMillis() + "";
				System.out.println("发送消息：" + message);
				client.send(ByteBuffer.wrap(message.getBytes()), new InetSocketAddress("127.0.0.1", 18888));
			} catch (NetException e) {
				e.printStackTrace();
			}
			ThreadUtils.sleep(1000);
		}
	}
	
}
