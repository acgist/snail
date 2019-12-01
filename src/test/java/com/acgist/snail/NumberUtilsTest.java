package com.acgist.snail;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import org.junit.Test;

import com.acgist.snail.utils.NumberUtils;
import com.acgist.snail.utils.StringUtils;

public class NumberUtilsTest extends BaseTest {

	@Test
	public void unsigned() {
		byte[] x = new byte[] {0, (byte) 0xd8, (byte) 0xf0};
		this.log(new BigInteger(x));
//		var number = new BigInteger("10000");
		var number = new BigInteger("-10000");
		this.log(StringUtils.hex(number.toByteArray()));
		this.log(new BigInteger(number.toByteArray()));
		var bytes = NumberUtils.encodeUnsigned(number, 100);
		this.log(StringUtils.hex(bytes));
		this.log(new BigInteger(bytes));
		this.log(NumberUtils.decodeUnsigned(ByteBuffer.wrap(bytes), bytes.length));
	}
	
	@Test
	public void bytes() {
		short value = (short) 18888;
//		short value = Short.MIN_VALUE;
//		short value = Short.MAX_VALUE;
		byte[] bytes;
		bytes = ByteBuffer.allocate(2).putShort(value).array();
		this.log(StringUtils.hex(bytes));
		this.log(NumberUtils.bytesToShort(bytes));
		bytes = NumberUtils.shortToBytes(value);
		this.log(StringUtils.hex(bytes));
		this.log(ByteBuffer.wrap(bytes).getShort());
	}
	
}
