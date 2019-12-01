package com.acgist.snail;

import java.net.NetworkInterface;
import java.net.SocketException;

import org.junit.Test;

import com.acgist.snail.net.application.ApplicationClient;
import com.acgist.snail.pojo.message.ApplicationMessage;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.ThreadUtils;

public class NetTest extends BaseTest {

	@Test
	public void ip() {
		ApplicationClient client = ApplicationClient.newInstance();
		final var ok = client.connect();
		client.send(ApplicationMessage.text("测试"));
		this.log(ok);
		ThreadUtils.sleep(10000);
	}
	
	@Test
	public void test() throws SocketException {
		NetworkInterface.networkInterfaces().forEach(x -> {
			x.getInetAddresses().asIterator().forEachRemaining(v -> {
				this.log("地址：" + v);
				this.log(v.isAnyLocalAddress());
				this.log(v.isLoopbackAddress());
				this.log(v.isLinkLocalAddress());
				this.log(v.isSiteLocalAddress());
				this.log(v.isMulticastAddress());
			});
			this.log(x);
		});
		this.log(NetUtils.localHostName());
		this.log(NetUtils.localHostAddress());
		this.log(NetUtils.defaultNetworkInterface());
	}
	
	@Test
	public void ipv6() {
		String ipv6 = "fe80::f84b:bc3a:9556:683d";
		byte[] bytes = NetUtils.encodeIPv6(ipv6);
		this.log(StringUtils.hex(bytes));
		String value = NetUtils.decodeIPv6(bytes);
		this.log(value);
	}
	
}
