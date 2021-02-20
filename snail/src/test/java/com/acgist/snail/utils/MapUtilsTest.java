package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
	
}
