package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class MapUtilsTest extends Performance {

	@Test
	public void testHashMap() {
		final Map<String, String> map = new HashMap<String, String>(16);
		map.put("1", "1");
		map.put("12", "12");
		map.put("123", "123");
		assertNotNull(map);
		this.costed(100000, () -> {
			final Map<String, String> costed = new HashMap<String, String>(16 / 3 * 4 + 1);
			for (int index = 0; index < 16; index++) {
				costed.put(index + "", index + "");
			}
		});
		this.costed(100000, () -> {
			final Map<String, String> costed = new HashMap<String, String>(16);
			for (int index = 0; index < 16; index++) {
				costed.put(index + "", index + "");
			}
		});
		this.costed(100000, () -> {
			final Map<String, String> costed = new HashMap<String, String>(16, 1);
			for (int index = 0; index < 16; index++) {
				costed.put(index + "", index + "");
			}
		});
	}
	
}
