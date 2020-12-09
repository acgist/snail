package com.acgist.snail.format;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.PacketSizeException;
import com.acgist.snail.utils.Performance;

public class BEncodeDecodeTest extends Performance {

	@Test
	public void testEncodeDecode() throws PacketSizeException {
		BEncodeEncoder encoder = BEncodeEncoder.newInstance();
//		encoder.writeList(List.of("a", "b"));
//		encoder.writeMap(Map.of("1", "2"));
		encoder.newList().put(List.of("a", "b")).flush();
		encoder.newMap().put(Map.of("1", "2")).flush();
		encoder.write("xxxx".getBytes());
		String content = encoder.toString();
//		String content = encoder
//			.newList().put("1").put("2").flush()
//			.newMap().put("aa", "bb").put("cc", "dd").flush()
//			.write("xxxx".getBytes())
//			.toString();
		this.log(content);
		BEncodeDecoder decoder = BEncodeDecoder.newInstance(content.getBytes());
		decoder.nextList().forEach(value -> this.log(new String((byte[]) value)));
		decoder.nextMap().forEach((key, value) -> {
			if(value instanceof Long) {
				this.log(key + "=" + value);
			} else {
				this.log(key + "=" + new String((byte[]) value));
			}
		});
		this.log(decoder.oddString());
	}
	
	@Test
	public void testNull() throws PacketSizeException {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("a", 1);
		map.put("b", null);
		map.put("c", "c");
		map.put(null, "c");
		String content = new String(BEncodeEncoder.encodeMap(map));
		this.log(content);
		var decoder = BEncodeDecoder.newInstance(content);
		decoder.nextMap().forEach((key, value) -> {
			if(value instanceof Number) {
				this.log(key + "-" + value);
			} else {
				this.log(key + "-" + new String((byte[]) value));
			}
		});
	}
	
	@Test
	public void testEncode() {
		this.log(BEncodeEncoder.encodeMapString(Map.of("1", "2")));
	}
	
}
