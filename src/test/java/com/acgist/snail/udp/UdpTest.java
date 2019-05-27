package com.acgist.snail.udp;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.junit.Test;

import com.acgist.snail.net.UdpClient;
import com.acgist.snail.net.UdpServer;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.ThreadUtils;

public class UdpTest {

	@Test
	public void client() {
		final int port = 18888;
		InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1", port);
		UdpTestMessageHandler handler = new UdpTestMessageHandler();
		UdpServer<UdpTestAcceptHandler> server = new UdpServer<>(port, "TestServer", UdpTestAcceptHandler.getInstance());
		server.handler();
		UdpClient<UdpTestMessageHandler> client = new UdpClient<UdpTestMessageHandler>("TestClient", handler, socketAddress) {
			@Override
			public boolean open() {
				return this.open(server.channel());
			}
		};
		client.open(server.channel());
		UdpClient<UdpTestMessageHandler> clients = new UdpClient<UdpTestMessageHandler>("TestClient", handler, socketAddress) {
			@Override
			public boolean open() {
				return this.open(server.channel());
			}
		};
		clients.open(server.channel());
		while (true) {
			try {
				final String message = "-";
//				最大长度
//				final String sendMessage = message.repeat((2 << 15) - 29);
				final String sendMessage = message.repeat(1);
				System.out.println("发送消息：" + sendMessage);
				System.out.println("消息长度：" + sendMessage.getBytes().length);
				client.send(ByteBuffer.wrap((sendMessage).getBytes()));
				clients.send(ByteBuffer.wrap((sendMessage).getBytes()));
			} catch (NetException e) {
				e.printStackTrace();
			}
//			ThreadUtils.sleep(Long.MAX_VALUE);
			ThreadUtils.sleep(1000);
		}
	}
	
}
