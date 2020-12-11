package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ModifyOptionalTest {

	@Test
	public void test() {
		ModifyOptional<String> optional = ModifyOptional.newInstance();
		assertEquals(null, optional.get());
		assertEquals("null", optional.get("null"));
	}
	
}
