package com.acgist.snail;

import java.nio.ByteBuffer;

import org.junit.Test;

import com.acgist.snail.net.torrent.peer.bootstrap.crypt.MSEPaddingReader;

public class PaddingMatcherTest {

	@Test
	public void cos() {
		long begin = System.currentTimeMillis();
		for (int index = 0; index < 100000; index++) {
			match();
		}
		System.out.println(System.currentTimeMillis() - begin);
	}
	
	@Test
	public void match() {
		MSEPaddingReader matcher = MSEPaddingReader.newInstance(2);
		ByteBuffer buffer = ByteBuffer.allocate(100);
		buffer.putShort((short) 2).put("12".getBytes());
//		buffer.putShort((short) 0);
		buffer.putShort((short) 100);
		buffer.put("1234".getBytes());
		buffer.flip();
		
		boolean ok = matcher.read(buffer);
		System.out.println(ok);
		System.out.println(matcher);
		
		ByteBuffer append = ByteBuffer.allocate(96);
		append.put("0".repeat(96).getBytes());
		append.flip();
		
		ok = matcher.read(append);
		System.out.println(ok);
		System.out.println(matcher);
		
		matcher.allPadding().forEach(bytes -> {
			System.out.println(new String(bytes));
		});
	}
	
}
