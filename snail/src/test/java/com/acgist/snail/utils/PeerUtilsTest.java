package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

class PeerUtilsTest extends Performance {

	@Test
	void testReadIPv4() {
		final ByteBuffer buffer = ByteBuffer.allocate(12);
		buffer.putInt(NetUtils.ipToInt("192.168.1.1"));
		buffer.putShort(NetUtils.portToShort(18888));
		buffer.putInt(NetUtils.ipToInt("192.168.1.100"));
		buffer.putShort(NetUtils.portToShort(28888));
		buffer.flip();
		final var map = PeerUtils.readIPv4(buffer);
		this.log(map);
		assertEquals(2, map.size());
	}
	
	@Test
	void testReadIPv6() {
		final ByteBuffer buffer = ByteBuffer.allocate(36);
		buffer.put(NetUtils.ipToBytes("fe80::f84b:bc3a:9556:683d"));
		buffer.putShort(NetUtils.portToShort(18888));
		buffer.put(NetUtils.ipToBytes("fe80::f84b:bc3a:9556:683f"));
		buffer.putShort(NetUtils.portToShort(28888));
		buffer.flip();
		final var map = PeerUtils.readIPv6(buffer);
		this.log(map);
		assertEquals(2, map.size());
	}
	
	@Test
	void testAllowedFast() {
		final String ip = "80.4.4.200";
		final byte[] infoHash = StringUtils.unhex("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		final int[] trueValues = {1059, 431, 808, 1217, 287, 376, 1188, 353, 508, 1246};
		int[] values = PeerUtils.allowedFast(1, ip, infoHash);
		assertEquals(1, values.length);
		values = PeerUtils.allowedFast(1313, ip, infoHash);
		assertArrayEquals(trueValues, values);
		this.log("Piece索引：{}", values);
	}
	
	@Test
	void testUrlEncode() {
		final byte[] source = {0x01, (byte) 0xFF, 0x12, 0x34, 0x56, 0x78, (byte) 0x9A, (byte) 0xBC, (byte) 0xDE, (byte) 0xF1, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF, 0x12, 0x34, 0x56, 0x78, (byte) 0x9A};
		final String target = "%01%FF%124Vx%9A%BC%DE%F1%23Eg%89%AB%CD%EF%124Vx%9A";
		this.log(PeerUtils.urlEncode(source));
		assertTrue(StringUtils.equalsIgnoreCase(target, PeerUtils.urlEncode(source)));
		final var bytes = ArrayUtils.random(20);
		this.log(new String(bytes));
		final String value = PeerUtils.urlEncode(bytes);
		this.log(value);
		this.costed(100000, () -> PeerUtils.urlEncode(bytes));
	}
	
}
