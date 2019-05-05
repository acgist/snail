package com.acgist.main;

import org.junit.Test;

import com.acgist.snail.net.dht.DhtClient;
import com.acgist.snail.utils.ThreadUtils;

public class DhtServerTest {

	private static final String host = "127.0.0.1";
	private static final int port = 18888; // 本地DHT测试端口
	
	@Test
	public void ping() {
		DhtClient client = DhtClient.newInstance(host, port);
		boolean ping = client.ping();
		System.out.println(ping);
		ThreadUtils.sleep(Long.MAX_VALUE);
	}
	
	@Test
	public void findNode() {
		DhtClient client = DhtClient.newInstance(host, port);
		final String target = "5E5324691812CAA0032EA76E813CCFC4D04E7E9E";
		client.findNode(target);
		ThreadUtils.sleep(Long.MAX_VALUE);
	}
	
}
