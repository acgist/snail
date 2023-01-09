package com.acgist.snail.context.wrapper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.utils.Performance;

class KeyValueWrapperTest extends Performance {

	@Test
	void testEncode() {
		var wrapper = KeyValueWrapper.newInstance(Map.of("1", "2", "3", "4"));
		var value = wrapper.encode();
		assertTrue("1=2&3=4".equals(value) || "3=4&1=2".equals(value));
		wrapper = KeyValueWrapper.newInstance(Map.of("a", "b", "3", "4"));
		value = wrapper.encode();
		assertTrue("a=b&3=4".equals(value) || "3=4&a=b".equals(value));
	}
	
	@Test
	void testDecode() {
		var wrapper = KeyValueWrapper.newInstance("1=2&").decode();
		assertEquals("2", wrapper.get("1"));
		wrapper = KeyValueWrapper.newInstance("&1=2&").decode();
		assertEquals("2", wrapper.get("1"));
		wrapper = KeyValueWrapper.newInstance("1=2&3").decode();
		assertEquals("2", wrapper.get("1"));
		assertEquals("", wrapper.get("3"));
		wrapper = KeyValueWrapper.newInstance("1=2&3=").decode();
		assertEquals("2", wrapper.get("1"));
		assertEquals("", wrapper.get("3"));
		wrapper = KeyValueWrapper.newInstance("1=2&3=4").decode();
		assertEquals("2", wrapper.get("1"));
		assertEquals("4", wrapper.get("3"));
		wrapper = KeyValueWrapper.newInstance("1=2 & 3=4").decode();
		assertEquals("2", wrapper.get("1"));
		assertEquals("4", wrapper.get("3"));
		wrapper = KeyValueWrapper.newInstance("a=a&B=B&c=C").decode();
		assertEquals("a", wrapper.get("a"));
		assertEquals(null, wrapper.get("b"));
		assertEquals("b", wrapper.get("b", "b"));
		assertEquals("C", wrapper.get("c"));
		assertEquals(null, wrapper.get("A"));
		assertEquals("B", wrapper.get("B"));
		assertEquals(null, wrapper.get("C"));
		assertEquals("a", wrapper.getIgnoreCase("a"));
		assertEquals("B", wrapper.getIgnoreCase("b"));
		assertEquals("C", wrapper.getIgnoreCase("c"));
		wrapper = KeyValueWrapper.newInstance(SymbolConfig.Symbol.COMMA.toChar(), SymbolConfig.Symbol.DOT.toChar(), "a.a,B.B,c.C").decode();
		assertEquals("a", wrapper.get("a"));
		assertEquals(null, wrapper.get("b"));
		assertEquals("C", wrapper.get("c"));
		assertEquals(null, wrapper.get("A"));
		assertEquals("B", wrapper.get("B"));
		assertEquals(null, wrapper.get("C"));
		assertEquals("a", wrapper.getIgnoreCase("a"));
		assertEquals("B", wrapper.getIgnoreCase("b"));
		assertEquals("C", wrapper.getIgnoreCase("c"));
	}

	@Test
	void testCosted() {
		assertDoesNotThrow(() -> {
			this.costed(100000, this::testEncode);
			this.costed(100000, this::testDecode);
		});
	}
	
}
