package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.NetworkInterface;
import java.net.SocketException;

import org.junit.jupiter.api.Test;

public class NetUtilsTest extends Performance {

	@Test
	public void testNetUtils() {
		assertNotNull(NetUtils.LOCAL_HOST_ADDRESS);
	}
	
	@Test
	public void testPort() {
		for (int port = 0; port < NetUtils.MAX_PORT; port++) {
			final short portShort = NetUtils.portToShort(port);
			assertEquals(port, NetUtils.portToInt(portShort));
		}
	}
	
	@Test
	public void testIPv4() {
		assertThrows(IllegalArgumentException.class, () -> NetUtils.ipToInt("1.1.1.1.1"));
		int ipInt = (int) 2130706433L;
		String ip = "127.0.0.1";
		assertEquals(ip, NetUtils.intToIP(ipInt));
		assertEquals(ipInt, NetUtils.ipToInt(ip));
		this.log(NetUtils.intToIP(ipInt));
		this.log(NetUtils.ipToInt(ip));
		ipInt = (int) 4294967295L;
		ip = "255.255.255.255";
		assertEquals(ip, NetUtils.intToIP(ipInt));
		assertEquals(ipInt, NetUtils.ipToInt(ip));
		this.log(NetUtils.intToIP(ipInt));
		this.log(NetUtils.ipToInt(ip));
		ipInt = (int) 0L;
		ip = "0.0.0.0";
		assertEquals(ip, NetUtils.intToIP(ipInt));
		assertEquals(ipInt, NetUtils.ipToInt(ip));
		this.log(NetUtils.intToIP(ipInt));
		this.log(NetUtils.ipToInt(ip));
		ipInt = (int) 2155905152L;
		ip = "128.128.128.128";
		assertEquals(ip, NetUtils.intToIP(ipInt));
		assertEquals(ipInt, NetUtils.ipToInt(ip));
		this.log(NetUtils.intToIP(ipInt));
		this.log(NetUtils.ipToInt(ip));
		for (int ipIndex = Short.MIN_VALUE; ipIndex < Short.MAX_VALUE; ipIndex++) {
			final String ipAddress = NetUtils.intToIP(ipIndex);
			assertEquals(ipIndex, NetUtils.ipToInt(ipAddress));
		}
		this.costed(100000, () -> NetUtils.ipToInt("128.128.128.128"));
		this.costed(100000, () -> NetUtils.intToIP((int) 2155905152L));
	}

	@Test
	public void testIPv6() {
		final String ipv6Address = "fe80::f84b:bc3a:9556:683d";
		final byte[] ipv6Bytes = NetUtils.ipToBytes(ipv6Address);
		final String ipv6Value = NetUtils.bytesToIP(ipv6Bytes);
		assertArrayEquals(ipv6Bytes, NetUtils.ipToBytes(ipv6Value));
		this.log(ipv6Value);
		this.log(StringUtils.hex(ipv6Bytes));
		final byte[] ipv4Bytes = NumberUtils.intToBytes((int) 2155905152L);
		this.costed(100000, () -> NetUtils.ipToBytes("128.128.128.128"));
		this.costed(100000, () -> NetUtils.bytesToIP(ipv4Bytes));
		this.costed(100000, () -> NetUtils.ipToBytes(ipv6Value));
		this.costed(100000, () -> NetUtils.bytesToIP(ipv6Bytes));
	}
	
	@Test
	public void testIP() {
		assertTrue(NetUtils.ip("192.168.1.1"));
		assertTrue(NetUtils.ip("192.168.1.100"));
		assertTrue(NetUtils.ip("999.999.999.999"));
		assertFalse(NetUtils.ip(".168.1.1"));
		assertFalse(NetUtils.ip("192.168.1.1000"));
		assertTrue(NetUtils.ip("::"));
		assertTrue(NetUtils.ip("::0001"));
		assertTrue(NetUtils.ip("2409:8054:48::1006/128"));
		assertTrue(NetUtils.ip("fe80:0000:0000:0000:0204:61ff:fe9d:f156"));
		assertTrue(NetUtils.ip("fe80:0:0:0:204:61ff:fe9d:f156"));
		assertTrue(NetUtils.ip("fe80::204:61ff:fe9d:f156"));
		assertTrue(NetUtils.ip("fe80:0000:0000:0000:0204:61ff:254.157.241.86"));
		assertTrue(NetUtils.ip("fe80:0:0:0:0204:61ff:254.157.241.86"));
		assertTrue(NetUtils.ip("fe80::204:61ff:254.157.241.86"));
		assertTrue(NetUtils.ip("fe80::c86:25ef:e78f:5479%19"));
		assertTrue(NetUtils.ip("fe80::c86:25ef:e78f:5479%19".toUpperCase()));
		assertTrue(NetUtils.ip("::ffff:192.168.89.9"));
		assertTrue(NetUtils.ip("0000:0000:0000:0000:0000:ffff:c0a8:5909"));
		assertTrue(NetUtils.ip("0000:0000:0000:0000:0000:ffff:c0a8:5909".toUpperCase()));
		assertTrue(NetUtils.ip("::ffff:10.0.0.1"));
		assertTrue(NetUtils.ip("::0102:f001"));
		assertTrue(NetUtils.ip("::1.2.240.1"));
		assertFalse(NetUtils.ip(":::1.2.240.1"));
		this.costed(100000, () -> NetUtils.ip("192.168.1.1"));
		this.costed(100000, () -> NetUtils.ip("0000:0000:0000:0000:0000:ffff:c0a8:5909"));
	}
	
	@Test
	public void testLan() {
		assertTrue(NetUtils.lan("192.168.1.1"));
		assertTrue(NetUtils.lan("192.168.1.100"));
		assertFalse(NetUtils.lan("192.168.100.100"));
		assertFalse(NetUtils.lan("114.114.114.114"));
		this.costed(100000, () -> NetUtils.lan("192.168.1.100"));
	}
	
	@Test
	public void testLocalIP() {
		this.log(NetUtils.LOCAL_HOST_ADDRESS);
		assertTrue(NetUtils.localIP("10.0.0.0"));
		assertTrue(NetUtils.localIP("172.16.0.0"));
		assertTrue(NetUtils.localIP("192.168.0.0"));
		assertTrue(NetUtils.localIP("127.0.0.0"));
		assertTrue(NetUtils.localIP("169.254.0.0"));
		assertTrue(NetUtils.localIP("224.0.0.0"));
		assertTrue(NetUtils.localIP("0:0:0:0:0:0:0:1"));
		assertTrue(NetUtils.localIP("fe80::c86:25ef:e78f:5479%19"));
		assertFalse(NetUtils.localIP("114.114.114.114"));
		this.costed(100000, () -> NetUtils.localIP("192.168.1.100"));
	}
	
	@Test
	public void testNetwork() throws SocketException {
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
			this.log("网卡：{}", networkInterfaces);
		});
		assertNotNull(NetUtils.LOCAL_HOST_ADDRESS);
	}
	
}
