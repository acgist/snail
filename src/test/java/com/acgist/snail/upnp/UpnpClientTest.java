package com.acgist.snail.upnp;

import org.junit.Test;

import com.acgist.snail.net.upnp.UpnpClient;
import com.acgist.snail.net.upnp.bootstrap.UpnpRequest;
import com.acgist.snail.net.upnp.bootstrap.UpnpResponse;
import com.acgist.snail.system.config.ProtocolConfig.Protocol;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.ThreadUtils;

public class UpnpClientTest {

	@Test
	public void upnp() {
		UpnpClient client = UpnpClient.newInstance();
		client.config();
		ThreadUtils.sleep(Long.MAX_VALUE);
	}

	@Test
	public void request() {
		UpnpRequest request = UpnpRequest.newRequest("urn:schemas-upnp-org:service:WANIPConnection:1");
//		String xml = request.buildGetExternalIPAddress();
//		String xml = request.buildGetSpecificPortMappingEntry(8080, Protocol.TCP);
		String xml = request.buildAddPortMapping(8080, NetUtils.inetHostAddress(), 8080, Protocol.tcp);
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
