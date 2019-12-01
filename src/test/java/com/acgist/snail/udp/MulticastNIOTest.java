package com.acgist.snail.udp;

import java.net.InetSocketAddress;

import org.junit.Test;

import com.acgist.snail.BaseTest;
import com.acgist.snail.net.UdpClient;
import com.acgist.snail.net.UdpServer;
import com.acgist.snail.net.upnp.UpnpServer;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.ThreadUtils;

public class MulticastNIOTest extends BaseTest {
	
	@Test
	public void server() {
		UdpServer<UdpAcceptHandlerTest> server = new UdpServer<>(UpnpServer.UPNP_PORT, "TestServer", UdpAcceptHandlerTest.getInstance()) {};
		server.join(2, UpnpServer.UPNP_HOST);
		server.handle();
		this.pause();
	}

	@Test
	public void client() throws NetException {
		InetSocketAddress socketAddress = new InetSocketAddress(UpnpServer.UPNP_HOST, UpnpServer.UPNP_PORT);
		UdpMessageHandlerTest handler = new UdpMessageHandlerTest();
		UdpClient<UdpMessageHandlerTest> client = new UdpClient<UdpMessageHandlerTest>("TestClient", handler, socketAddress) {
			@Override
			public boolean open() {
				return this.open(PORT_AUTO);
			}
		};
//		client.join(2, UpnpServer.UPNP_HOST);
		client.send("你好");
		ThreadUtils.sleep(1000);
	}

}
