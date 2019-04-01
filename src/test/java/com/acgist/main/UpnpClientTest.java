package com.acgist.main;

import org.junit.Test;

import com.acgist.snail.net.upnp.UpnpClient;
import com.acgist.snail.net.upnp.UpnpRequest;
import com.acgist.snail.net.upnp.UpnpResponse;
import com.acgist.snail.net.upnp.UpnpService.Protocol;
import com.acgist.snail.utils.NetUtils;

public class UpnpClientTest {

	@Test
	public void test() throws InterruptedException {
		UpnpClient client = new UpnpClient();
		client.upnp();
		Thread.sleep(10 * 1000);
	}

	@Test
	public void request() {
		UpnpRequest request = UpnpRequest.newRequest("urn:schemas-upnp-org:service:WANIPConnection:1");
//		String xml = request.buildGetExternalIPAddress();
//		String xml = request.buildGetSpecificPortMappingEntry(8080, Protocol.TCP);
		String xml = request.buildAddPortMapping(8080, NetUtils.inetHostAddress(), Protocol.TCP);
		System.out.println(xml);
	}
	
	@Test
	public void response() {
		String xml = "<?xml version=\"1.0\"?>"
		+ "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
		+ "	<SOAP-ENV:Body>"
		+ "		<u:GetExternalIPAddressResponse xmlns:u=\"urn:schemas-upnp-org:service:WANIPConnection:1\">"
		+ "			<NewExternalIPAddress>219.137.140.38</NewExternalIPAddress>"
		+ "		</u:GetExternalIPAddressResponse>"
		+ "	</SOAP-ENV:Body>"
		+ "</SOAP-ENV:Envelope>";
		System.out.println(UpnpResponse.parseGetExternalIPAddress(xml));
	}
	
}
