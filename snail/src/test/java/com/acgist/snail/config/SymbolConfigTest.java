package com.acgist.snail.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.torrent.lsd.LocalServiceDiscoveryServer;
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
	
	@Test
	void testJoin() {
		Object[] args = null;
		assertEquals(null, SymbolConfig.Symbol.AND.join(args));
		assertEquals("", SymbolConfig.Symbol.AND.join(""));
		assertEquals("1", SymbolConfig.Symbol.AND.join("1"));
		assertEquals("1&2", SymbolConfig.Symbol.AND.join("1", "2"));
		assertEquals("1&2&3", SymbolConfig.Symbol.AND.join("1", "2", 3));
		assertEquals("1&2&null&3", SymbolConfig.Symbol.AND.join("1", "2", null, "3"));
		assertEquals("1&2&null&3", SymbolConfig.Symbol.AND.join("1", "2", null, 3));
		assertEquals(LocalServiceDiscoveryServer.lsdHost() + ":" + LocalServiceDiscoveryServer.LSD_PORT, SymbolConfig.Symbol.COLON.join(LocalServiceDiscoveryServer.lsdHost(), LocalServiceDiscoveryServer.LSD_PORT));
		assertEquals(LocalServiceDiscoveryServer.lsdHost() + SymbolConfig.Symbol.COLON.toString() + LocalServiceDiscoveryServer.LSD_PORT, SymbolConfig.Symbol.COLON.join(LocalServiceDiscoveryServer.lsdHost(), LocalServiceDiscoveryServer.LSD_PORT));
	}
	
	@Test
	void testCosted() {
		String key = "key";
		String value = "value";
		this.costed(1000000, () -> {
			String x = key + ":" + value;
			assertNotNull(x);
		});
		this.costed(1000000, () -> {
			String x = SymbolConfig.Symbol.COLON.join(key, value);
			assertNotNull(x);
		});
	}
	
}
