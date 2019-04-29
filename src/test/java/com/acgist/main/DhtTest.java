package com.acgist.main;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.acgist.snail.net.dht.DhtClient;
import com.acgist.snail.system.bcode.BCodeEncoder;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.ThreadUtils;

public class DhtTest {

	@Test
	public void client() {
		InetSocketAddress address = new InetSocketAddress("127.0.0.1", SystemConfig.getDhtPort());
		DhtClient client = DhtClient.newInstance(address);
		while (true) {
			try {
				final Map<String, String> data = new HashMap<String, String>();
				data.put("v", "1.0.0");
				System.out.println("发送消息：" + data);
				client.send(ByteBuffer.wrap(BCodeEncoder.mapToBytes(data)), address);
			} catch (NetException e) {
				e.printStackTrace();
			}
			ThreadUtils.sleep(1000);
		}
	}
	
	@Test
	public void ping() {
		String host = "127.0.0.1";
		Integer port = 49160; // FDM测试端口
		DhtClient client = DhtClient.newInstance(host, port);
		client.ping();
		ThreadUtils.sleep(Long.MAX_VALUE);
	}
	
}
