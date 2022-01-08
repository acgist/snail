package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class NumberUtilsTest extends Performance {

	@Test
	void testBuild() {
		assertNotEquals(NumberUtils.build(), NumberUtils.build());
	}
	
	@Test
	void testBit() {
		final byte byteValue = -2;
		final int intValue = byteValue;
		this.log(byteValue);
		this.log(intValue);
		this.log(byteValue & 0xFF);
		this.log(Integer.toHexString(byteValue));
		this.log(Integer.toHexString(intValue));
		this.log(Integer.toHexString(byteValue & 0xFF));
		assertEquals(intValue, byteValue);
	}

	@Test
	void testTransition() {
		for (short index = Short.MIN_VALUE; index < Short.MAX_VALUE; index++) {
			if(((byte) index) != ((byte) (index & 0xFF))) {
				this.log(index);
			}
			assertEquals((byte) index, (byte) (index & 0xFF));
			assertEquals(((int) index) & 0xFFFF, (int) (index & 0xFFFF));
			assertEquals(Short.toUnsignedInt(index), (int) (index & 0xFFFF));
		}
	}
	
	@Test
	void testLong() {
		final ByteBuffer buffer = ByteBuffer.allocate(8);
		for (long index = Short.MIN_VALUE; index < Short.MAX_VALUE; index++) {
			buffer.clear();
			buffer.putLong(index);
			final byte[] bytes = NumberUtils.longToBytes(index);
			assertTrue(Arrays.equals(buffer.array(), bytes));
			assertEquals(index, NumberUtils.bytesToLong(bytes));
		}
		for (long index = Long.MIN_VALUE; index < Long.MIN_VALUE + Short.MAX_VALUE; index++) {
			buffer.clear();
			buffer.putLong(index);
			final byte[] bytes = NumberUtils.longToBytes(index);
			assertTrue(Arrays.equals(buffer.array(), bytes));
			assertEquals(index, NumberUtils.bytesToLong(bytes));
		}
		for (long index = Long.MAX_VALUE - Short.MAX_VALUE; index < Long.MAX_VALUE; index++) {
			buffer.clear();
			buffer.putLong(index);
			final byte[] bytes = NumberUtils.longToBytes(index);
			assertTrue(Arrays.equals(buffer.array(), bytes));
			assertEquals(index, NumberUtils.bytesToLong(bytes));
		}
	}
	
	@Test
	void testInt() {
		final ByteBuffer buffer = ByteBuffer.allocate(4);
		for (int index = Short.MIN_VALUE; index < Short.MAX_VALUE; index++) {
			buffer.clear();
			buffer.putInt(index);
			final byte[] bytes = NumberUtils.intToBytes(index);
			assertTrue(Arrays.equals(buffer.array(), bytes));
			assertEquals(index, NumberUtils.bytesToInt(bytes));
		}
		for (int index = Integer.MIN_VALUE; index < Integer.MIN_VALUE + Short.MAX_VALUE; index++) {
			buffer.clear();
			buffer.putInt(index);
			final byte[] bytes = NumberUtils.intToBytes(index);
			assertTrue(Arrays.equals(buffer.array(), bytes));
			assertEquals(index, NumberUtils.bytesToInt(bytes));
			assertEquals(index, NumberUtils.bytesToInt(bytes));
		}
		for (int index = Integer.MAX_VALUE - Short.MAX_VALUE; index < Integer.MAX_VALUE; index++) {
			buffer.clear();
			buffer.putInt(index);
			final byte[] bytes = NumberUtils.intToBytes(index);
			assertTrue(Arrays.equals(buffer.array(), bytes));
			assertEquals(index, NumberUtils.bytesToInt(bytes));
		}
	}
	
	@Test
	void testShort() {
		final ByteBuffer buffer = ByteBuffer.allocate(2);
		for (short index = Short.MIN_VALUE; index < Short.MAX_VALUE; index++) {
			buffer.clear();
			buffer.putShort(index);
			final byte[] bytes = NumberUtils.shortToBytes(index);
			assertTrue(Arrays.equals(buffer.array(), bytes));
			assertEquals(index, NumberUtils.bytesToShort(bytes));
		}
	}
	
	@Test
	void testCeilDiv() {
		assertEquals(1, NumberUtils.ceilDiv(2, 2));
		assertEquals(2, NumberUtils.ceilDiv(3, 2));
		assertEquals(2, NumberUtils.ceilDiv(4, 2));
	}
	
	@Test
	void testCeilMult() {
		assertEquals(2, NumberUtils.ceilMult(2, 2));
		assertEquals(4, NumberUtils.ceilMult(3, 2));
		assertEquals(4, NumberUtils.ceilMult(4, 2));
	}
	
	@RepeatedTest(10)
	void testUnsigned() {
		final int length = 100;
		final var random = NumberUtils.random();
		final byte[] bytes = new byte[length];
		random.nextBytes(bytes);
		final var source = new BigInteger(1, bytes);
		final var decode = NumberUtils.decodeBigInteger(ByteBuffer.wrap(bytes), length);
		this.log(decode);
		this.log(source);
		assertEquals(source, decode);
		final var encode = NumberUtils.encodeBigInteger(decode, length);
		assertArrayEquals(bytes, encode);
		assertEquals(source, new BigInteger(1, encode));
		assertEquals(source, NumberUtils.decodeBigInteger(ByteBuffer.wrap(encode), length));
	}
	
	@Test
	void testEquals() {
		assertFalse(NumberUtils.equals(null, Integer.valueOf(100000)));
		assertFalse(NumberUtils.equals(Integer.valueOf(100000), null));
		assertFalse(NumberUtils.equals(Integer.valueOf(100000), Integer.valueOf(100010)));
		assertTrue(NumberUtils.equals(Integer.valueOf(100000), Integer.valueOf(100000)));
		assertFalse(NumberUtils.equals(Integer.valueOf(100000), Long.valueOf(100000)));
	}
	
}
