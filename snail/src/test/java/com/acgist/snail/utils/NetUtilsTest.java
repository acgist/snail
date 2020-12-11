package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.NetworkInterface;
import java.net.SocketException;

import org.junit.jupiter.api.Test;

public class NetUtilsTest extends Performance {

	@Test
	public void ip() {
		this.log(NetUtils.longToIP(2130706433));
		this.log(NetUtils.ipToLong("127.1.1.1"));
	}
	
	@Test
	public void testAddress() throws SocketException {
		NetworkInterface.networkInterfaces().forEach(networkInterfaces -> {
			networkInterfaces.getInterfaceAddresses().stream().forEach(networkInterface -> {
				final var address = networkInterface.getAddress();
				this.log("地址：{}-{}-{}-{}-{}-{}-{}-{}",
					networkInterface,
					networkInterface.getBroadcast(),
					networkInterface.getNetworkPrefixLength(),
					address.isAnyLocalAddress(),
					address.isLoopbackAddress(),
					address.isLinkLocalAddress(),
					address.isSiteLocalAddress(),
					address.isMulticastAddress()
				);
			});
			this.log(networkInterfaces);
		});
		this.log(NetUtils.localHostName());
		this.log(NetUtils.localHostAddress());
		this.log(NetUtils.defaultNetworkInterface());
	}
	
	@Test
	public void testIPv6() {
		String ipv6 = "fe80::f84b:bc3a:9556:683d";
		byte[] bytes = NetUtils.bytesToIP(ipv6);
		this.log(StringUtils.hex(bytes));
		String value = NetUtils.ipToBytes(bytes);
		this.log(value);
	}
	
	@Test
	public void testGateway() {
		assertTrue(NetUtils.gateway("192.168.1.100"));
		assertFalse(NetUtils.gateway("192.168.2.100"));
	}

	@Test
	public void testCosted() {
		this.costed(100000, () -> NetUtils.gateway("192.168.1.100"));
	}
	
}
