package com.acgist.snail.bcode;

import org.junit.Test;

import com.acgist.snail.system.bcode.BCodeDecoder;
import com.acgist.snail.system.bcode.BCodeEncoder;

public class BCodeTest {

	@Test
	public void test() {
		BCodeEncoder encoder = BCodeEncoder.newInstance();
//		encoder.build(List.of("a", "b", 0));
//		encoder.build(Map.of("1", "2"));
//		String content = encoder.toString();
		String content = encoder.newList().put("1").put("2").flush()
			.newMap().put("a", "b").put("c", "d").flush().toString();
		System.out.println(content);
		BCodeDecoder decoder = BCodeDecoder.newInstance(content.getBytes());
		System.out.println(decoder.nextList());
		System.out.println(decoder.nextMap());
	}
	
}
