package com.acgist.snail.upnp;

import org.junit.Test;

import com.acgist.snail.net.upnp.bootstrap.UpnpService;
import com.acgist.snail.system.config.ProtocolConfig.Protocol;
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
		System.out.println(UpnpService.getInstance().getSpecificPortMappingEntry(18888, Protocol.tcp));
	}

	@Test
	public void addPortMapping() throws NetException {
		UpnpService.getInstance().load(serviceUrl);
		System.out.println(UpnpService.getInstance().addPortMapping(18888, 18888, Protocol.tcp));
	}

	@Test
	public void deletePortMapping() throws NetException {
		UpnpService.getInstance().load(serviceUrl);
		System.out.println(UpnpService.getInstance().deletePortMapping(18888, Protocol.tcp));
	}
	
	@Test
	public void setting() throws NetException {
		UpnpService.getInstance().load(serviceUrl);
		UpnpService.getInstance().setting();
	}

}
