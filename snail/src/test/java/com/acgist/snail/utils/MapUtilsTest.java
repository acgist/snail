package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

class MapUtilsTest extends Performance {

	@Test
	void testMapUtils() {
		assertTrue(MapUtils.isEmpty(Map.of()));
		assertFalse(MapUtils.isEmpty(Map.of("1", "2")));
		assertFalse(MapUtils.isNotEmpty(Map.of()));
		assertTrue(MapUtils.isNotEmpty(Map.of("1", "2")));
	}

	@Test
	void TestToUrlQuery() {
		Map<String, String> map = null;
		assertNull(MapUtils.toUrlQuery(map));
		map = Map.of();
		assertNull(MapUtils.toUrlQuery(map));
		map = Map.of("1", "2", "3", "测试");
		assertNotNull(MapUtils.toUrlQuery(map));
		this.log(MapUtils.toUrlQuery(map));
	}
	
	@Test
	void testCosted() {
		final Map<String, Object> map = Map.of("map", Map.of("key", "value"));
		assertNotNull(MapUtils.getMap(map, "map"));
		this.costed(100000, () -> MapUtils.getMap(map, "map"));
	}
	
}
