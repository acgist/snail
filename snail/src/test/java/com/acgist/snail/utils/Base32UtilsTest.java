package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class Base32UtilsTest extends Performance {

	@Test
	public void testBase32Utils() {
		assertDoesNotThrow(() -> {
			Base32Utils.encode((String) null);
			Base32Utils.encode((byte[]) null);
			Base32Utils.decode(null);
			Base32Utils.decodeToString(null);
		});
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
