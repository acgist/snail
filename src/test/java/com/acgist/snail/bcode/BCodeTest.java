package com.acgist.snail.bcode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.acgist.snail.system.bencode.BEncodeDecoder;
import com.acgist.snail.system.bencode.BEncodeEncoder;

public class BCodeTest {

	@Test
	public void test() {
		BEncodeEncoder encoder = BEncodeEncoder.newInstance();
		encoder.build(List.of("a", "b"));
		encoder.build(Map.of("1", "2"));
		encoder.append("xxxx".getBytes());
		String content = encoder.toString();
//		String content = encoder.newList().put("1").put("2").flush()
//			.newMap().put("aa", "b").put("c", "dd").flush().toString();
		System.out.println(content);
		BEncodeDecoder decoder = BEncodeDecoder.newInstance(content.getBytes());
		decoder.nextList().forEach(value -> System.out.println(new String((byte[]) value)));
		decoder.nextMap().forEach((key, value) -> {
			System.out.println(key + "=" + new String((byte[]) value));
		});
//		System.out.println(decoder.oddBytes());
		System.out.println(decoder.oddString());
	}
	
	@Test
	public void nullTest() {
		Map<String, String> map = new HashMap<>();
		map.put("test", null);
		System.out.println(new String(BEncodeEncoder.encodeMap(map)));
	}
	
}
