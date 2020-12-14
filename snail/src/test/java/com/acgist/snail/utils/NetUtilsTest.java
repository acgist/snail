package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

import org.junit.jupiter.api.Test;

public class NetUtilsTest extends Performance {

	@Test
	public void ip() {
		this.log(NetUtils.longToIP(2130706433));
		this.log(NetUtils.ipToLong("127.1.1.1"));
	}

	@Test
	public void testLocalAddress() {
		this.log(NetUtils.localIPAddress("114.114.114.114"));
		this.log(NetUtils.localIPAddress("10.0.0.0"));
		this.log(NetUtils.localIPAddress("172.16.0.0"));
		this.log(NetUtils.localIPAddress("192.168.0.0"));
		this.log(NetUtils.localIPAddress("127.0.0.0"));
		this.log(NetUtils.localIPAddress("169.254.0.0"));
		this.log(NetUtils.localIPAddress("224.0.0.0"));
		this.log(NetUtils.localIPAddress("0:0:0:0:0:0:0:1"));
	}
	
	@Test
	public void testLocalIPAddress() {
		this.log(NetUtils.localIPAddress("10.0.0.0"));
		this.log(NetUtils.localIPAddress("172.16.0.0"));
		this.log(NetUtils.localIPAddress("192.168.0.0"));
		this.log(NetUtils.localIPAddress("127.0.0.0"));
		this.log(NetUtils.localIPAddress("169.254.0.0"));
		this.log(NetUtils.localIPAddress("224.0.0.0"));
		this.log(NetUtils.localIPAddress("114.114.114.114"));
		this.log(NetUtils.localIPAddress("0:0:0:0:0:0:0:1"));
		this.log(NetUtils.localIPAddress("fe80::c86:25ef:e78f:5479%19"));
	}
	
	@Test
	public void testInetAddress() throws UnknownHostException {
		List.of(
			InetAddress.getByName("10.0.0.0"),
			InetAddress.getByName("172.16.0.0"),
			InetAddress.getByName("192.168.0.0"),
			InetAddress.getByName("127.0.0.0"),
			InetAddress.getByName("169.254.0.0"),
			InetAddress.getByName("224.0.0.0"),
			InetAddress.getByName("114.114.114.114"),
			InetAddress.getByName("0:0:0:0:0:0:0:1"),
			InetAddress.getByName("fe80::c86:25ef:e78f:5479%19")
		).forEach(address -> {
			this.log(
				"{}-{}-{}-{}-{}-{}",
				address.isAnyLocalAddress(),
				address.isLoopbackAddress(),
				address.isLinkLocalAddress(),
				address.isMulticastAddress(),
				address.isSiteLocalAddress(),
				address.toString()
			);
		});
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
	public void testCostedGateway() {
		this.costed(100000, () -> NetUtils.gateway("192.168.1.100"));
	}
	
	@Test
	public void testCostedLocalIPAddress() {
		this.costed(100000, () -> NetUtils.localIPAddress("192.168.1.100"));
	}
	
}
