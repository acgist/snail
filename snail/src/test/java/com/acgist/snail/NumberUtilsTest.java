package com.acgist.snail;

import java.nio.ByteBuffer;

import org.junit.Test;

import com.acgist.snail.system.config.CryptConfig;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.NumberUtils;
import com.acgist.snail.utils.StringUtils;

public class NumberUtilsTest extends BaseTest {

	@Test
	public void testUnsigned() {
		var random = NumberUtils.random();
		final byte[] bytes = new byte[CryptConfig.PRIVATE_KEY_LENGTH];
		for (int index = 0; index < CryptConfig.PRIVATE_KEY_LENGTH; index++) {
			bytes[index] = (byte) random.nextInt(SystemConfig.UNSIGNED_BYTE_MAX);
		}
		var value = NumberUtils.decodeBigInteger(ByteBuffer.wrap(bytes), CryptConfig.PRIVATE_KEY_LENGTH);
		this.log(value);
		this.log(NumberUtils.encodeBigInteger(value, CryptConfig.PUBLIC_KEY_LENGTH));
	}
	
	@Test
	public void testBytes() {
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
	
	@Test
	public void testEquals() {
		this.log(NumberUtils.equals(null, Integer.valueOf(100000)));
		this.log(NumberUtils.equals(Integer.valueOf(100000), null));
		this.log(NumberUtils.equals(Integer.valueOf(100000), Integer.valueOf(100010)));
		this.log(NumberUtils.equals(Integer.valueOf(100000), Integer.valueOf(100000)));
	}
	
}
