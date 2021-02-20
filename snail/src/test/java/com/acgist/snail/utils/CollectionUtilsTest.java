package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class CollectionUtilsTest {

	@Test
	void testEmpty() {
		assertTrue(CollectionUtils.isEmpty(List.of()));
		assertFalse(CollectionUtils.isEmpty(List.of("1", "2")));
		assertFalse(CollectionUtils.isNotEmpty(List.of()));
		assertTrue(CollectionUtils.isNotEmpty(List.of("1", "2")));
	}
	
}
