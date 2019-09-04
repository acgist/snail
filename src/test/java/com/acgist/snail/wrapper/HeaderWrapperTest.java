package com.acgist.snail.wrapper;

import org.junit.Test;

import com.acgist.snail.net.upnp.UpnpServer;
import com.acgist.snail.pojo.wrapper.HeaderWrapper;

public class HeaderWrapperTest {

	private static final String NEW_LINE = "\r\n";
	
	@Test
	public void test() {
		final StringBuilder builder = new StringBuilder();
		builder
			.append("M-SEARCH * HTTP/1.1").append(NEW_LINE)
			.append("HOST: ").append(UpnpServer.UPNP_HOST).append(":").append(UpnpServer.UPNP_PORT).append(NEW_LINE)
			.append("MX: 3").append(NEW_LINE)
			.append("ST: urn:schemas-upnp-org:device:InternetGatewayDevice:1").append(NEW_LINE)
			.append("MAN: \"ssdp:discover\"").append(NEW_LINE)
			.append(NEW_LINE);
		System.out.println(builder.toString());
		HeaderWrapper wrapper = HeaderWrapper.newBuilder("M-SEARCH * HTTP/1.1");
		wrapper
			.header("HOST", UpnpServer.UPNP_HOST + ":" + UpnpServer.UPNP_PORT)
			.header("MX", "3")
			.header("ST", "urn:schemas-upnp-org:device:InternetGatewayDevice:1")
			.header("MAN", "\"ssdp:discover\"");
		System.out.println(wrapper.build());
	}
	
}
