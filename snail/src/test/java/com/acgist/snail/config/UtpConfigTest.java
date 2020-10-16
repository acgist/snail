package com.acgist.snail.config;

import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import com.acgist.snail.BaseTest;

public class UtpConfigTest extends BaseTest {

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
		this.cost();
		IntStream.range(0, 10000000).forEach(index -> {
			UtpConfig.Type.of((byte) 1);
		});
		this.costed();
		IntStream.range(0, 10000000).forEach(index -> {
			UtpConfig.Type.of((byte) 65);
		});
		this.costed();
	}
	
}
