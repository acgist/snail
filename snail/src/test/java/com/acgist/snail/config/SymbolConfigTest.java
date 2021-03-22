package com.acgist.snail.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

class SymbolConfigTest extends Performance {

	@Test
	void testSymbolConfig() {
		final var values = SymbolConfig.Symbol.values();
		for (SymbolConfig.Symbol symbol : values) {
			assertEquals(symbol + "", symbol.toString());
			this.log(symbol);
			this.log(symbol.toChar());
			this.log(symbol.toString());
		}
	}
	
}
