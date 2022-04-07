package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class Base32UtilsTest extends Performance {

	@Test
	void testBase32Utils() {
		var content = Base32Utils.encode(StringUtils.unhex("fa493c8add6d907a0575631831033dcf94ba5217"));
		assertEquals("7JETZCW5NWIHUBLVMMMDCAZ5Z6KLUUQX", content);
		assertEquals("fa493c8add6d907a0575631831033dcf94ba5217", StringUtils.hex(Base32Utils.decode(content)));
		assertEquals("f1f030f304fe5a0e88cdec1fff3da7f3bf557f17", StringUtils.hex(Base32Utils.decode("6HYDB4YE7ZNA5CGN5QP76PNH6O7VK7YX".toLowerCase())));
		assertDoesNotThrow(() -> {
			Base32Utils.encode((String) null);
			Base32Utils.encode((byte[]) null);
			Base32Utils.decode(null);
			Base32Utils.decodeToString(null);
		});
		for (int index = Short.MIN_VALUE; index < Short.MAX_VALUE; index++) {
			final String encode = Base32Utils.encode(String.valueOf(index));
			assertEquals(String.valueOf(index), Base32Utils.decodeToString(encode));
		}
		String value = Base32Utils.encode("123456");
		assertEquals("GEZDGNBVGY", value);
		assertEquals("123456", Base32Utils.decodeToString(value));
		value = Base32Utils.encode("测试");
		assertEquals("422YX2FPSU", value);
		assertEquals("测试", Base32Utils.decodeToString(value));
		value = Base32Utils.encode("acgist");
		assertEquals("MFRWO2LTOQ", value);
		assertEquals("acgist", Base32Utils.decodeToString(value));
		this.costed(100000, () -> {
			final String encode = Base32Utils.encode("123456");
			Base32Utils.decodeToString(encode);
		});
	}
	
}
