package com.acgist.main;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.junit.Test;

import com.acgist.snail.utils.ThreadUtils;

public class RandomAccessFileTest {

	@Test
	public void test() throws IOException {
		RandomAccessFile stream = new RandomAccessFile("e://tmp.txt", "rwd");
		stream.write(2);
		stream.seek(1000);
		byte[] bytes = new byte[1024];
		stream.read(bytes);
		System.out.println(stream.length());
		ThreadUtils.sleep(Long.MAX_VALUE);
		stream.close();
	}
	
}
