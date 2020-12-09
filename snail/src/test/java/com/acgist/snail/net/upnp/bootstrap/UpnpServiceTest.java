package com.acgist.snail.net.upnp.bootstrap;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.utils.Performance;

public class UpnpServiceTest extends Performance {

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
