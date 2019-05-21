package com.acgist.main;

import org.junit.Test;

import com.acgist.snail.net.upnp.bootstrap.UpnpService;
import com.acgist.snail.net.upnp.bootstrap.UpnpService.Protocol;
import com.acgist.snail.system.exception.NetException;

/**
 * serviceUrl通过UPNPClient获取
 */
public class UpnpServiceTest {

	private String serviceUrl = "http://192.168.1.1:10087/rootDesc.xml";
	
	@Test
	public void getExternalIPAddress() throws NetException {
		UpnpService.getInstance().load(serviceUrl);
		System.out.println(UpnpService.getInstance().getExternalIPAddress());
	}

	@Test
	public void getSpecificPortMappingEntry() throws NetException {
		UpnpService.getInstance().load(serviceUrl);
		System.out.println(UpnpService.getInstance().getSpecificPortMappingEntry(17888, Protocol.TCP));
	}

	@Test
	public void addPortMapping() throws NetException {
		UpnpService.getInstance().load(serviceUrl);
		System.out.println(UpnpService.getInstance().addPortMapping(17888, 18888, Protocol.TCP));
	}

	@Test
	public void deletePortMapping() throws NetException {
		UpnpService.getInstance().load(serviceUrl);
		System.out.println(UpnpService.getInstance().deletePortMapping(17888, Protocol.TCP));
	}

}
