package com.acgist.snail;

import java.nio.ByteBuffer;

import org.junit.Test;

import com.acgist.snail.utils.StringUtils;

public class ByteBufferTest {

	@Test
	public void readContent() {
		ByteBuffer buffer = ByteBuffer.wrap("1234".getBytes());
		buffer.compact();
//		buffer.flip();
		System.out.println(buffer);
		System.out.println(StringUtils.ofByteBuffer(buffer));
	}
	
	@Test
	public void append() {
		ByteBuffer buffer = ByteBuffer.allocate(100);
		buffer.put("1234".getBytes());
		System.out.println(buffer);
		buffer.flip();
		System.out.println(buffer);
		buffer.put("5678".getBytes());
		System.out.println(buffer);
		buffer.flip();
		System.out.println(buffer);
		buffer.compact();
		System.out.println(buffer);
	}
	
	@Test
	public void put() {
		ByteBuffer buffer = ByteBuffer.wrap("1234".getBytes());
		ByteBuffer x = ByteBuffer.allocate(4);
		System.out.println(buffer);
		System.out.println(x);
		x.put(buffer);
		System.out.println(buffer);
		System.out.println(x);
	}
	
	@Test
	public void cos() {
		long begin = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			match();
		}
		System.out.println("消耗：" + (System.currentTimeMillis() - begin));
	}
	
	@Test
	public void match() {
		int index = 0;
		String name = "123456";
//		ByteBuffer buffer = ByteBuffer.wrap("1123456".getBytes());
		ByteBuffer buffer = ByteBuffer.wrap("11233456".getBytes());
//		ByteBuffer buffer = ByteBuffer.wrap("112231234e56".getBytes());
		byte[] bytes = name.getBytes();
		final int length = bytes.length;
		if(buffer.remaining() < length) {
			buffer.compact();
			System.out.println("不匹配");
			print(buffer);
			return;
		}
		while(length > index) {
			if(buffer.get() != bytes[index]) {
				buffer.position(buffer.position() - index); // 最开始的位置移动一位继续匹配
				index = 0; // 注意位置
				if(buffer.remaining() < length) {
					break;
				}
			} else {
				index++;
			}
		}
		if(index == length) { // 匹配
			buffer.position(buffer.position() - length);
			buffer.compact();
			System.out.println("匹配");
			print(buffer);
		} else { // 不匹配
			buffer.compact();
			System.out.println("不匹配");
			print(buffer);
		}
	}
	
	private void print(ByteBuffer buffer) {
		System.out.println(buffer);
		buffer.flip();
		byte[] v = new byte[buffer.remaining()];
		buffer.get(v);
		System.out.println(new String(v));
	}
	
}
