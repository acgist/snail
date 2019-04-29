package com.acgist.main;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.junit.Test;

import com.acgist.snail.net.dht.DhtClient;
import com.acgist.snail.net.dht.DhtServer;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.ThreadUtils;

public class DhtTest {

	@Test
	public void server() {
		DhtServer.getInstance().listen();
		ThreadUtils.sleep(Long.MAX_VALUE);
	}

	@Test
	public void client() {
		DhtClient client = DhtClient.getInstance();
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
