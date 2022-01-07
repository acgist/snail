package com.acgist.snail.logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TupleTest {

	@Test
	void testTuple() {
		Tuple tuple = new Tuple("1234");
		assertEquals("1234", tuple.format("a", "b"));
		tuple = new Tuple("1234{}4321{}");
		assertEquals("1234a4321{}", tuple.format("a"));
		assertEquals("1234a4321b", tuple.format("a", "b"));
		assertEquals("1234a4321b", tuple.format("a", "b", "c"));
		tuple = new Tuple("{}1234{}4321{}");
		assertEquals("a1234{}4321{}", tuple.format("a"));
		assertEquals("a1234b4321{}", tuple.format("a", "b"));
		assertEquals("a1234b4321c", tuple.format("a", "b", "c"));
		tuple = new Tuple("{}1234{}4321");
		assertEquals("a1234{}4321", tuple.format("a"));
		assertEquals("a1234b4321", tuple.format("a", "b"));
		assertEquals("a1234b4321", tuple.format("a", "b", "c"));
	}
	
}
