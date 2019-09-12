package com.acgist.snail;

import java.nio.ByteBuffer;

import org.junit.Test;

public class ByteBufferTest {

	@Test
	public void match() {
		String name = "123456";
		ByteBuffer buffer = ByteBuffer.wrap("1123123d4567890".getBytes());
		byte[] bytes = name.getBytes();
		int index = 0;
		final int length = bytes.length;
//		buffer.flip();
		while(buffer.remaining() >= (length - index) && length > index) {
			if(buffer.get() != bytes[index]) {
				index = 0;
			} else {
				index++;
			}
		}
		if(index == length) { // 匹配
			System.out.println(buffer);
			buffer.position(buffer.position() - length);
			buffer.compact();
			System.out.println("匹配");
			System.out.println(buffer);
			buffer.flip();
			byte[] v = new byte[buffer.remaining()];
			buffer.get(v);
			System.out.println(new String(v));
		} else { // 不匹配
			buffer.compact();
			System.out.println("不匹配");
			System.out.println(buffer);
		}
	}
	
}
