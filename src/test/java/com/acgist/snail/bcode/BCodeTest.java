package com.acgist.snail.bcode;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.acgist.snail.system.bencode.BEncodeDecoder;
import com.acgist.snail.system.bencode.BEncodeEncoder;

public class BCodeTest {

	@Test
	public void test() {
		BEncodeEncoder encoder = BEncodeEncoder.newInstance();
//		encoder.build(List.of("a", "b", 0));
//		encoder.build(Map.of("1", "2"));
//		String content = encoder.toString();
		String content = encoder.newList().put("1").put("2").flush()
			.newMap().put("a", "b").put("c", "d").flush().toString();
		System.out.println(content);
		BEncodeDecoder decoder = BEncodeDecoder.newInstance(content.getBytes());
		System.out.println(decoder.nextList());
		System.out.println(decoder.nextMap());
	}
	
	@Test
	public void nullTest() {
		Map<String, String> map = new HashMap<>();
		map.put("test", null);
		System.out.println(new String(BEncodeEncoder.encodeMap(map)));
	}
	
}
