package com.acgist.snail.pojo.wrapper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.wrapper.DescriptionWrapper;
import com.acgist.snail.utils.Performance;

class DescriptionWrapperTest extends Performance {

	@Test
	void testEncode() {
		var wrapper = DescriptionWrapper.newEncoder(List.of("acgist", "蜗牛"));
		assertEquals("l6:acgist6:蜗牛e", wrapper.serialize());
		wrapper = DescriptionWrapper.newEncoder(null);
		assertNull(wrapper.serialize());
		wrapper = DescriptionWrapper.newEncoder(List.of());
		assertNull(wrapper.serialize());
	}
	
	@Test
	void testDecode() {
		var wrapper = DescriptionWrapper.newDecoder("l6:acgist6:蜗牛e");
		assertEquals(2, wrapper.deserialize().size());
		wrapper = DescriptionWrapper.newDecoder(null);
		assertTrue(wrapper.deserialize().isEmpty());
		wrapper = DescriptionWrapper.newDecoder("");
		assertTrue(wrapper.deserialize().isEmpty());
	}

	@Test
	void testCosted() {
		assertDoesNotThrow(() -> {
			this.costed(100000, () -> {
				testEncode();
				testDecode();
			});
		});
	}
	
}
