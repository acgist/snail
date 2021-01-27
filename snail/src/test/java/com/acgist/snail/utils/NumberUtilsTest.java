package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

public class NumberUtilsTest extends Performance {

	@Test
	public void testBuild() {
		assertNotEquals(NumberUtils.build(), NumberUtils.build());
	}

	@Test
	public void testLong() {
		final long value = System.currentTimeMillis();
		final byte[] bytes = NumberUtils.longToBytes(value);
		this.costed(100000, () -> {
			final ByteBuffer buffer = ByteBuffer.allocate(8);
			buffer.putLong(System.currentTimeMillis());
		});
		this.costed(100000, () -> {
			NumberUtils.longToBytes(value);
		});
		this.costed(100000, () -> {
			final ByteBuffer buffer = ByteBuffer.wrap(bytes);
			buffer.getLong();
		});
		this.costed(100000, () -> {
			NumberUtils.bytesToLong(bytes);
		});
		this.costed(100000, () -> {
			final long costedValue = System.nanoTime();
			final ByteBuffer costedBuffer = ByteBuffer.allocate(8);
			final byte[] costedBytes = costedBuffer.array();
			costedBuffer.putLong(costedValue);
			assertArrayEquals(costedBytes, NumberUtils.longToBytes(costedValue));
			assertEquals(costedValue, NumberUtils.bytesToLong(costedBytes));
		});
	}
	
	@Test
	public void testInt() {
		this.costed(100000, () -> {
			final int value = (int) (System.nanoTime() % Integer.MAX_VALUE);
			final ByteBuffer buffer = ByteBuffer.allocate(4);
			final byte[] bytes = buffer.array();
			buffer.putInt(value);
			assertArrayEquals(bytes, NumberUtils.intToBytes(value));
			assertEquals(value, NumberUtils.bytesToInt(bytes));
		});
	}
	
	@Test
	public void testShort() {
		this.costed(100000, () -> {
			final short value = (short) (System.nanoTime() % Short.MAX_VALUE);
			final ByteBuffer buffer = ByteBuffer.allocate(2);
			final byte[] bytes = buffer.array();
			buffer.putShort(value);
			assertArrayEquals(bytes, NumberUtils.shortToBytes(value));
			assertEquals(value, NumberUtils.bytesToShort(bytes));
		});
	}
	
	@Test
	public void testCeilDiv() {
		assertEquals(1, NumberUtils.ceilDiv(2, 2));
		assertEquals(2, NumberUtils.ceilDiv(3, 2));
		assertEquals(2, NumberUtils.ceilDiv(4, 2));
	}
	
	@Test
	public void testCeilMult() {
		assertEquals(2, NumberUtils.ceilMult(2, 2));
		assertEquals(4, NumberUtils.ceilMult(3, 2));
		assertEquals(4, NumberUtils.ceilMult(4, 2));
	}
	
	@RepeatedTest(10)
	public void testUnsigned() {
		final int length = 100;
		final var random = NumberUtils.random();
		final byte[] bytes = new byte[length];
		random.nextBytes(bytes);
		final var source = new BigInteger(1, bytes);
		final var decode = NumberUtils.decodeBigInteger(ByteBuffer.wrap(bytes), length);
		this.log(decode);
		this.log(source);
		assertTrue(source.equals(decode));
		final var encode = NumberUtils.encodeBigInteger(decode, length);
		assertArrayEquals(bytes, encode);
		assertEquals(source, new BigInteger(1, encode));
		assertEquals(source, NumberUtils.decodeBigInteger(ByteBuffer.wrap(encode), length));
	}
	
	@Test
	public void testEquals() {
		assertFalse(NumberUtils.equals(null, Integer.valueOf(100000)));
		assertFalse(NumberUtils.equals(Integer.valueOf(100000), null));
		assertFalse(NumberUtils.equals(Integer.valueOf(100000), Integer.valueOf(100010)));
		assertTrue(NumberUtils.equals(Integer.valueOf(100000), Integer.valueOf(100000)));
		assertFalse(NumberUtils.equals(Integer.valueOf(100000), Long.valueOf(100000)));
	}
	
}
