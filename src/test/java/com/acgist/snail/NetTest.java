package com.acgist.snail;

import java.net.NetworkInterface;
import java.net.SocketException;

import org.junit.Test;

import com.acgist.snail.net.application.ApplicationClient;
import com.acgist.snail.pojo.message.ApplicationMessage;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.ThreadUtils;

public class NetTest {

	@Test
	public void ip() {
		ApplicationClient client = ApplicationClient.newInstance();
		final var ok = client.connect();
		client.send(ApplicationMessage.text("测试"));
		System.out.println(ok);
		ThreadUtils.sleep(10000);
	}
	
	@Test
	public void test() throws SocketException {
		NetworkInterface.networkInterfaces().forEach(x -> {
			x.getInetAddresses().asIterator().forEachRemaining(v -> {
				System.out.println("地址：" + v);
				System.out.println(v.isAnyLocalAddress());
				System.out.println(v.isLoopbackAddress());
				System.out.println(v.isLinkLocalAddress());
				System.out.println(v.isSiteLocalAddress());
				System.out.println(v.isMulticastAddress());
			});
			System.out.println(x);
		});
		System.out.println(NetUtils.localHostName());
		System.out.println(NetUtils.localHostAddress());
		System.out.println(NetUtils.defaultNetworkInterface());
	}
	
}
