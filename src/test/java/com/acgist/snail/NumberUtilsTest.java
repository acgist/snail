package com.acgist.snail;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import org.junit.Test;

import com.acgist.snail.utils.NumberUtils;

public class NumberUtilsTest {

	@Test
	public void unsigned() {
		var number = new BigInteger("-10000");
		var bytes = NumberUtils.encodeUnsigned(number, 100);
		System.out.println(new BigInteger(bytes));
		System.out.println(NumberUtils.decodeUnsigned(ByteBuffer.wrap(number.toByteArray()), 1));
	}
	
}
