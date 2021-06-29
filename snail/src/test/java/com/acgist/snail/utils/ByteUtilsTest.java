package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

class ByteUtilsTest extends Performance {

	@Test
	void testEmpty() {
		final ByteBuffer buffer = ByteBuffer.allocate(0);
		assertFalse(buffer.hasRemaining());
		final var bytes = ByteUtils.remainingToBytes(buffer);
		final var string = ByteUtils.remainingToString(buffer);
		assertArrayEquals(new byte[] {}, bytes);
		assertEquals("", string);
	}
	
	@Test
	void testRemainingToBytes() {
		final var source = "这是一个测试".getBytes();
		final ByteBuffer buffer = ByteBuffer.wrap(source);
		final var bytes = ByteUtils.remainingToBytes(buffer);
		assertArrayEquals(source, bytes);
	}
	
	@Test
	void testRemainingToString() {
		final var source = "这是一个测试";
		final ByteBuffer buffer = ByteBuffer.wrap(source.getBytes());
		final var bytes = ByteUtils.remainingToString(buffer);
		assertEquals(source, bytes);
	}
	
}
