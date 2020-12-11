package com.acgist.snail.config;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

public class UtpConfigTest extends Performance {

	@Test
	public void test() {
		UtpConfig.Type[] types = UtpConfig.Type.values();
		for (UtpConfig.Type type : types) {
			this.log(type.type());
			this.log(type.typeVersion() + "=" + Integer.toHexString(type.typeVersion()));
		}
	}
	
	@Test
	public void testCost() {
		this.costed(10000000, () -> UtpConfig.Type.of((byte) 1));
		this.costed(10000000, () -> UtpConfig.Type.of((byte) 65));
	}
	
}
