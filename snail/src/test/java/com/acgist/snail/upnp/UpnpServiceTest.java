package com.acgist.snail.upnp;

import org.junit.jupiter.api.Test;

import com.acgist.snail.BaseTest;
import com.acgist.snail.exception.NetException;
import com.acgist.snail.net.upnp.bootstrap.UpnpService;
import com.acgist.snail.protocol.Protocol;

public class UpnpServiceTest extends BaseTest {

	private String serviceUrl = "http://192.168.1.1:10087/rootDesc.xml";
//	private String serviceUrl = "http://192.168.1.1:5351/rootDesc.xml";
	
	@Test
	public void testGetExternalIPAddress() throws NetException {
		UpnpService.getInstance().load(this.serviceUrl);
		this.log(UpnpService.getInstance().getExternalIPAddress());
	}

	@Test
	public void testGetSpecificPortMappingEntry() throws NetException {
		UpnpService.getInstance().load(this.serviceUrl);
		this.log(UpnpService.getInstance().getSpecificPortMappingEntry(18888, Protocol.Type.TCP));
		this.log(UpnpService.getInstance().getSpecificPortMappingEntry(18888, Protocol.Type.UDP));
	}

	@Test
	public void testAddPortMapping() throws NetException {
		UpnpService.getInstance().load(this.serviceUrl);
		this.log(UpnpService.getInstance().addPortMapping(18888, 18888, Protocol.Type.TCP));
		this.log(UpnpService.getInstance().addPortMapping(18888, 18888, Protocol.Type.UDP));
	}

	@Test
	public void testDeletePortMapping() throws NetException {
		UpnpService.getInstance().load(this.serviceUrl);
		this.log(UpnpService.getInstance().deletePortMapping(18888, Protocol.Type.TCP));
		this.log(UpnpService.getInstance().deletePortMapping(18888, Protocol.Type.UDP));
	}
	
	@Test
	public void testMapping() throws NetException {
		UpnpService.getInstance().load(this.serviceUrl).mapping();
	}

}
