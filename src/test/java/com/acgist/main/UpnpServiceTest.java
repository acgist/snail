package com.acgist.main;

import org.junit.Test;

import com.acgist.snail.net.upnp.UpnpService;
import com.acgist.snail.net.upnp.UpnpService.Protocol;
import com.acgist.snail.utils.NetUtils;

public class UpnpServiceTest {

	@Test
	public void externalIPAddress() {
		UpnpService.getInstance().load("http://192.168.1.1:10087/rootDesc.xml");
		System.out.println(UpnpService.getInstance().getExternalIPAddress());
	}
	
	@Test
	public void specificPortMappingEntry() {
		UpnpService.getInstance().load("http://192.168.1.1:10087/rootDesc.xml");
		System.out.println(UpnpService.getInstance().getSpecificPortMappingEntry(8080, Protocol.TCP));
	}
	
	@Test
	public void addPortMapping() {
		UpnpService.getInstance().load("http://192.168.1.1:10087/rootDesc.xml");
		System.out.println(UpnpService.getInstance().addPortMapping(8080, NetUtils.inetHostAddress(), Protocol.TCP));
	}
	
	@Test
	public void deletePortMapping() {
		UpnpService.getInstance().load("http://192.168.1.1:10087/rootDesc.xml");
		System.out.println(UpnpService.getInstance().deletePortMapping(8080, Protocol.TCP));
	}
	
}
