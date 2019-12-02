package com.acgist.snail.upnp;

import org.junit.Test;

import com.acgist.snail.BaseTest;
import com.acgist.snail.net.upnp.bootstrap.UpnpService;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.system.exception.NetException;

public class UpnpServiceTest extends BaseTest {

	/**
	 * <p>使用MSearch查询</p>
	 */
	private String serviceUrl = "http://192.168.1.1:10087/rootDesc.xml";
	
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
