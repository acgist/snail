package com.acgist.snail.bcode;

import org.junit.Test;

import com.acgist.snail.system.bencode.BEnodeDecoder;
import com.acgist.snail.system.bencode.BEnodeEncoder;

public class BCodeTest {

	@Test
	public void test() {
		BEnodeEncoder encoder = BEnodeEncoder.newInstance();
//		encoder.build(List.of("a", "b", 0));
//		encoder.build(Map.of("1", "2"));
//		String content = encoder.toString();
		String content = encoder.newList().put("1").put("2").flush()
			.newMap().put("a", "b").put("c", "d").flush().toString();
		System.out.println(content);
		BEnodeDecoder decoder = BEnodeDecoder.newInstance(content.getBytes());
		System.out.println(decoder.nextList());
		System.out.println(decoder.nextMap());
	}
	
}
