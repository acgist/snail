package com.acgist.main;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import org.junit.Test;

import com.acgist.snail.net.UdpClient;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.ThreadUtils;

public class UDPTest {

	@Test
	public void client() {
		final int port = 18888;
		DatagramChannel channel = NetUtils.buildUdpChannel(port);
		UdpTestMessageHandler handler = new UdpTestMessageHandler();
		UdpClient.bindServerHandler(handler, channel);
		UdpClient<UdpTestMessageHandler> client = new UdpClient<UdpTestMessageHandler>("TestClient", handler) {
		};
		client.open(channel);
		UdpClient<UdpTestMessageHandler> clients = new UdpClient<UdpTestMessageHandler>("TestClient", handler) {
		};
		clients.open(channel);
		while (true) {
			try {
				final String message = System.currentTimeMillis() + "";
				final String sendMessage = message.repeat(1);
				System.out.println("发送消息：" + sendMessage);
				System.out.println("消息长度：" + sendMessage.length());
				client.send(ByteBuffer.wrap(sendMessage.getBytes()), new InetSocketAddress("127.0.0.1", port));
				clients.send(ByteBuffer.wrap(sendMessage.getBytes()), new InetSocketAddress("127.0.0.1", port));
			} catch (NetException e) {
				e.printStackTrace();
			}
//			ThreadUtils.sleep(Long.MAX_VALUE);
			ThreadUtils.sleep(1000);
		}
	}
	
}
