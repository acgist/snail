package com.acgist.snail;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import org.junit.Test;

import com.acgist.snail.utils.NumberUtils;
import com.acgist.snail.utils.StringUtils;

public class NumberUtilsTest {

	@Test
	public void unsigned() {
		byte[] x = new byte[] {0, (byte) 0xd8, (byte) 0xf0};
		System.out.println(new BigInteger(x));
//		var number = new BigInteger("10000");
		var number = new BigInteger("-10000");
		System.out.println(StringUtils.hex(number.toByteArray()));
		System.out.println(new BigInteger(number.toByteArray()));
		var bytes = NumberUtils.encodeUnsigned(number, 100);
		System.out.println(StringUtils.hex(bytes));
		System.out.println(new BigInteger(bytes));
		System.out.println(NumberUtils.decodeUnsigned(ByteBuffer.wrap(bytes), bytes.length));
	}
	
	@Test
	public void bytes() {
		short value = (short) 18888;
//		short value = Short.MIN_VALUE;
//		short value = Short.MAX_VALUE;
		byte[] bytes;
		bytes = ByteBuffer.allocate(2).putShort(value).array();
		System.out.println(StringUtils.hex(bytes));
		System.out.println(NumberUtils.bytesToShort(bytes));
		bytes = NumberUtils.shortToBytes(value);
		System.out.println(StringUtils.hex(bytes));
		System.out.println(ByteBuffer.wrap(bytes).getShort());
	}
	
}
