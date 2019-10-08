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
	
}
