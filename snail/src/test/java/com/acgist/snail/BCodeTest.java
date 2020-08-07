package com.acgist.snail;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.acgist.snail.system.exception.PacketSizeException;
import com.acgist.snail.system.format.BEncodeDecoder;
import com.acgist.snail.system.format.BEncodeEncoder;

public class BCodeTest extends BaseTest {

	@Test
	public void testReadWrite() throws PacketSizeException {
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
	public void testMap() throws PacketSizeException {
		var decoder = BEncodeDecoder.newInstance("d8:completei6e10:downloadedi17e10:incompletei0e8:intervali924e12:min intervali462e5:peers36:����m�Wj���LmA�s;I�ʆL��TTz�e");
		var map = decoder.nextMap();
		this.log(map);
	}
	
}
