package com.acgist.snail.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

class UtpConfigTest extends Performance {

	@Test
	void testType() {
		final UtpConfig.Type[] types = UtpConfig.Type.values();
		for (UtpConfig.Type type : types) {
			this.log("{}-{}-{}-{}", type, type.type(), type.typeVersion(), Integer.toHexString(type.typeVersion()));
		}
		assertEquals(0x00, UtpConfig.Type.DATA.type());
		assertEquals(0x01, UtpConfig.Type.DATA.typeVersion());
		assertEquals(0x04, UtpConfig.Type.SYN.type());
		assertEquals(0x41, UtpConfig.Type.SYN.typeVersion());
	}
	
}
