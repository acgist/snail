package com.acgist.snail.context.wrapper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

class DescriptionWrapperTest extends Performance {

	@Test
	void testEncode() {
		assertEquals("l6:acgist6:蜗牛e", DescriptionWrapper.newEncoder(List.of("acgist", "蜗牛")).serialize());
		assertNull(DescriptionWrapper.newEncoder(null).serialize());
		assertNull(DescriptionWrapper.newEncoder(List.of()).serialize());
	}
	
	@Test
	void testDecode() {
		final List<String> deserialize = DescriptionWrapper.newDecoder("l6:acgist6:蜗牛e").deserialize();
		assertEquals(2, deserialize.size());
		assertEquals("acgist", deserialize.get(0));
		assertEquals("蜗牛", deserialize.get(1));
		assertTrue(DescriptionWrapper.newDecoder(null).deserialize().isEmpty());
		assertTrue(DescriptionWrapper.newDecoder("").deserialize().isEmpty());
	}

	@Test
	void testCosted() {
		assertDoesNotThrow(() -> {
			this.costed(100000, () -> {
				this.testEncode();
				this.testDecode();
			});
		});
	}
	
}
