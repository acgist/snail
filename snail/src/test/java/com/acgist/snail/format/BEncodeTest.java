package com.acgist.snail.format;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.PacketSizeException;
import com.acgist.snail.utils.Performance;

class BEncodeTest extends Performance {

	@Test
	void testBEncode() throws PacketSizeException {
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
		final String odd = decoder.toString();
		this.log(odd);
		assertEquals("xxxx", odd);
		assertEquals(1, map.size());
		assertEquals(2, list.size());
		final String mix = BEncodeEncoder.newInstance()
			.newList().put(List.of("a", List.of("b", "c"), Map.of("d", "e"))).flush().toString();
		this.log(mix);
		assertEquals("l1:al1:b1:ced1:d1:eee", mix);
		final var mixList = BEncodeDecoder.newInstance(mix).nextList();
		assertEquals("a", new String((byte[]) mixList.get(0)));
		final List<?> mixListList = (List<?>) mixList.get(1);
		assertEquals("b", new String((byte[]) mixListList.get(0)));
		assertEquals("c", new String((byte[]) mixListList.get(1)));
		final Map<?, ?> mixListMap = (Map<?, ?>) mixList.get(2);
		assertEquals("e", new String((byte[]) mixListMap.get("d")));
	}
	
	@Test
	void testNull() throws PacketSizeException {
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
		assertEquals(map.size(), decodeMap.size());
	}
	
	@Test
	void testEncode() {
		final String map = BEncodeEncoder.encodeMapString(Map.of("1", "2"));
		final String list = BEncodeEncoder.encodeListString(List.of("1", "2"));
		assertEquals("d1:11:2e", map);
		assertEquals("l1:11:2e", list);
	}

	@Test
	void testException() throws PacketSizeException {
		assertThrows(IllegalArgumentException.class, () -> BEncodeDecoder.newInstance("l"));
	}
	
	@Test
	void testEmpty() throws PacketSizeException {
		var decoder = BEncodeDecoder.newInstance("d1:11:2e");
		assertTrue(decoder.isEmpty());
		decoder.nextType();
		assertTrue(decoder.isNotEmpty());
		decoder = BEncodeDecoder.newInstance("de");
		assertEquals(BEncodeDecoder.Type.MAP, decoder.nextType());
		assertTrue(decoder.isEmpty());
		decoder = BEncodeDecoder.newInstance("le");
		assertEquals(BEncodeDecoder.Type.LIST, decoder.nextType());
		assertTrue(decoder.isEmpty());
		decoder = BEncodeDecoder.newInstance("xx");
		decoder.nextType();
		assertTrue(decoder.isEmpty());
	}
	
	@Test
	void testNext() throws PacketSizeException {
		var decoder = BEncodeDecoder.newInstance("d1:11:2e").next();
		assertFalse(decoder.isEmpty());
		decoder = BEncodeDecoder.newInstance("de").next();
		assertTrue(decoder.isEmpty());
		decoder = BEncodeDecoder.newInstance("le").next();
		assertTrue(decoder.isEmpty());
	}
	
	@Test
	void testCosted() {
		final long costed = this.costed(100000, () -> {
			BEncodeEncoder.encodeMapString(Map.of("1", "2"));
			BEncodeEncoder.encodeListString(List.of("1", "2"));
		});
		assertTrue(costed < 1000);
	}
	
}
