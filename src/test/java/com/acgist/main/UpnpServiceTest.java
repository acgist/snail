package com.acgist.main;

import org.junit.Test;

import com.acgist.snail.net.upnp.UpnpService;
import com.acgist.snail.net.upnp.UpnpService.Protocol;
import com.acgist.snail.utils.NetUtils;

/**
 * serviceUrl通过UPNPClient获取
 */
public class UpnpServiceTest {

	private String serviceUrl = "http://192.168.1.1:10087/rootDesc.xml";
	
	@Test
	public void getExternalIPAddress() {
		UpnpService.getInstance().load(serviceUrl);
		System.out.println(UpnpService.getInstance().getExternalIPAddress());
	}

	@Test
	public void getSpecificPortMappingEntry() {
		UpnpService.getInstance().load(serviceUrl);
		System.out.println(UpnpService.getInstance().getSpecificPortMappingEntry(16688, Protocol.TCP));
	}

	@Test
	public void addPortMapping() {
		UpnpService.getInstance().load(serviceUrl);
		System.out.println(UpnpService.getInstance().addPortMapping(16688, NetUtils.inetHostAddress(), Protocol.TCP));
	}

	@Test
	public void deletePortMapping() {
		UpnpService.getInstance().load("http://192.168.1.1:10087/rootDesc.xml");
		System.out.println(UpnpService.getInstance().deletePortMapping(16688, Protocol.TCP));
	}

}
