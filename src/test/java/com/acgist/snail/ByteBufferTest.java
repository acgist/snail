package com.acgist.snail;

import java.nio.ByteBuffer;

import org.junit.Test;

import com.acgist.snail.utils.StringUtils;

public class ByteBufferTest extends BaseTest {

	@Test
	public void readContent() {
		ByteBuffer buffer = ByteBuffer.wrap("1234".getBytes());
		buffer.compact();
//		buffer.flip();
		this.log(buffer);
		this.log(StringUtils.ofByteBuffer(buffer));
	}
	
	@Test
	public void append() {
		ByteBuffer buffer = ByteBuffer.allocate(100);
		buffer.put("1234".getBytes());
		this.log(buffer);
		buffer.flip();
		this.log(buffer);
		buffer.put("5678".getBytes());
		this.log(buffer);
		buffer.flip();
		this.log(buffer);
		buffer.compact();
		this.log(buffer);
	}
	
	@Test
	public void put() {
		ByteBuffer buffer = ByteBuffer.wrap("1234".getBytes());
		ByteBuffer x = ByteBuffer.allocate(4);
		this.log(buffer);
		this.log(x);
		x.put(buffer);
		this.log(buffer);
		this.log(x);
	}
	
	@Test
	public void cos() {
		long begin = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			match();
		}
		this.log("消耗：" + (System.currentTimeMillis() - begin));
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
			this.log("不匹配");
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
			this.log("匹配");
			print(buffer);
		} else { // 不匹配
			buffer.compact();
			this.log("不匹配");
			print(buffer);
		}
	}
	
	private void print(ByteBuffer buffer) {
		this.log(buffer);
		buffer.flip();
		byte[] v = new byte[buffer.remaining()];
		buffer.get(v);
		this.log(new String(v));
	}
	
}
