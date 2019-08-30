package com.acgist.snail.udp;

import java.net.InetSocketAddress;

import org.junit.Test;

import com.acgist.snail.net.UdpClient;
import com.acgist.snail.net.UdpServer;
import com.acgist.snail.net.upnp.UpnpServer;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.ThreadUtils;

public class MulticastNIOTest {
	
	@Test
	public void server() {
		UdpServer<UdpTestAcceptHandler> server = new UdpServer<>(UpnpServer.UPNP_PORT, "TestServer", UdpTestAcceptHandler.getInstance()) {};
		server.join(2, UpnpServer.UPNP_HOST);
		server.handler();
		ThreadUtils.sleep(Long.MAX_VALUE);
	}

	@Test
	public void client() throws NetException {
		InetSocketAddress socketAddress = new InetSocketAddress(UpnpServer.UPNP_HOST, UpnpServer.UPNP_PORT);
		UdpTestMessageHandler handler = new UdpTestMessageHandler();
		UdpClient<UdpTestMessageHandler> client = new UdpClient<UdpTestMessageHandler>("TestClient", handler, socketAddress) {
			@Override
			public boolean open() {
				return this.open(NetUtils.buildUdpChannel(-1));
			}
		};
//		client.join(2, UpnpServer.UPNP_HOST);
		client.send("你好");
		ThreadUtils.sleep(1000);
	}

}
