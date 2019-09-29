package com.acgist.snail;

import java.nio.ByteBuffer;

import org.junit.Test;

import com.acgist.snail.utils.IoUtils;

public class IoUtilsTest {

	@Test
	public void readContent() {
		ByteBuffer buffer = ByteBuffer.wrap("1234".getBytes());
		buffer.compact();
//		buffer.flip();
		System.out.println(buffer);
		System.out.println(IoUtils.readContent(buffer));
	}
	
}
