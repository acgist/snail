package com.acgist.snail.format;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.PacketSizeException;
import com.acgist.snail.utils.Performance;

public class BEncodeTest extends Performance {

	@Test
	public void testBEncode() throws PacketSizeException {
		final BEncodeEncoder encoder = BEncodeEncoder.newInstance();
		encoder
			.newList().put(List.of("a", "b")).flush()
			.newMap().put(Map.of("1", "2")).flush()
			.write("xxxx".getBytes());
		final String content = encoder.toString();
		this.log(content);
		assertEquals("l1:a1:bed1:11:2exxxx", content);
		final BEncodeDecoder decoder = BEncodeDecoder.newInstance(content.getBytes());
		final var list = decoder.nextList();
		list.forEach(value -> this.log(new String((byte[]) value)));
		final var map = decoder.nextMap();
		map.forEach((key, value) -> {
				if(value instanceof Long) {
					this.log(key + "=" + value);
				} else {
					this.log(key + "=" + new String((byte[]) value));
				}
			});
		final String odd = decoder.oddString();
		this.log(odd);
		assertEquals("xxxx", odd);
		assertTrue(map.size() == 1);
		assertTrue(list.size() == 2);
	}
	
	@Test
	public void testNull() throws PacketSizeException {
		final Map<String, Object> map = new LinkedHashMap<>();
		map.put("a", 1);
		map.put("b", null);
		map.put("c", "c");
		map.put("d", "");
		map.put(null, "c");
		final String content = new String(BEncodeEncoder.encodeMap(map));
		this.log(content);
		assertEquals("d1:ai1e1:b0:1:c1:c1:d0:0:1:ce", content);
		final var decoder = BEncodeDecoder.newInstance(content);
		final var decodeMap = decoder.nextMap();
		decodeMap.forEach((key, value) -> {
			if(value instanceof Number) {
				this.log(key + "-" + value);
			} else {
				this.log(key + "-" + new String((byte[]) value));
			}
		});
		assertTrue(map.size() == decodeMap.size());
	}
	
	@Test
	public void testEncode() {
		final String map = BEncodeEncoder.encodeMapString(Map.of("1", "2"));
		final String list = BEncodeEncoder.encodeListString(List.of("1", "2"));
		assertEquals("d1:11:2e", map);
		assertEquals("l1:11:2e", list);
	}

	@Test
	public void testCosted() {
		final long costed = this.costed(100000, () -> {
			BEncodeEncoder.encodeMapString(Map.of("1", "2"));
			BEncodeEncoder.encodeListString(List.of("1", "2"));
		});
		assertTrue(costed < 1000);
	}
	
}
