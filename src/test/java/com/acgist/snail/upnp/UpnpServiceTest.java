package com.acgist.snail.upnp;

import org.junit.Test;

import com.acgist.snail.BaseTest;
import com.acgist.snail.net.upnp.bootstrap.UpnpService;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.system.exception.NetException;

/**
 * serviceUrl通过UPNPClient获取
 */
public class UpnpServiceTest extends BaseTest {

	private String serviceUrl = "http://192.168.1.1:10087/rootDesc.xml";
	
	@Test
	public void getExternalIPAddress() throws NetException {
		UpnpService.getInstance().load(this.serviceUrl);
		this.log(UpnpService.getInstance().getExternalIPAddress());
	}

	@Test
	public void getSpecificPortMappingEntry() throws NetException {
		UpnpService.getInstance().load(this.serviceUrl);
		this.log(UpnpService.getInstance().getSpecificPortMappingEntry(18888, Protocol.Type.TCP));
		this.log(UpnpService.getInstance().getSpecificPortMappingEntry(18888, Protocol.Type.UDP));
	}

	@Test
	public void addPortMapping() throws NetException {
		UpnpService.getInstance().load(this.serviceUrl);
		this.log(UpnpService.getInstance().addPortMapping(18888, 18888, Protocol.Type.TCP));
		this.log(UpnpService.getInstance().addPortMapping(18888, 18888, Protocol.Type.UDP));
	}

	@Test
	public void deletePortMapping() throws NetException {
		UpnpService.getInstance().load(this.serviceUrl);
		this.log(UpnpService.getInstance().deletePortMapping(18888, Protocol.Type.TCP));
		this.log(UpnpService.getInstance().deletePortMapping(18888, Protocol.Type.UDP));
	}
	
	@Test
	public void setting() throws NetException {
		UpnpService.getInstance().load(this.serviceUrl);
		UpnpService.getInstance().mapping();
	}

}
