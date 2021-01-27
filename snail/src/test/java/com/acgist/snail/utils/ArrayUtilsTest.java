package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class ArrayUtilsTest extends Performance {

	@Test
	public void testRandom() {
		this.log(new byte[10]);
		this.log(ArrayUtils.random(10));
		final byte[] value = new byte[10];
		NumberUtils.random().nextBytes(value);
		this.log(value);
		assertNotNull(ArrayUtils.random(10));
		this.costed(100000, () -> ArrayUtils.random(10));
		this.costed(100000, () -> {
			final var random = NumberUtils.random();
			final byte[] bytes = new byte[10];
			random.nextBytes(bytes);
		});
	}
	
}
