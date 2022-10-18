package com.acgist.snail.config;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
	void testSplit() {
		assertArrayEquals(new String[] { "1" }, SymbolConfig.Symbol.COMMA.split("1"));
		assertArrayEquals(new String[] { "", "1" }, SymbolConfig.Symbol.COMMA.split(",1"));
		assertArrayEquals(new String[] { "1", "" }, SymbolConfig.Symbol.COMMA.split("1,"));
		assertArrayEquals(new String[] { "1", "2" }, SymbolConfig.Symbol.COMMA.split("1,2"));
		assertArrayEquals(new String[] { "1", "", "2" }, SymbolConfig.Symbol.COMMA.split("1,,2"));
		assertArrayEquals(new String[] { "1", " ", "2" }, SymbolConfig.Symbol.COMMA.split("1, ,2"));
		assertArrayEquals(new String[] { "1" }, SymbolConfig.Symbol.COMMA.split("1", SymbolConfig.FullType.PREFIX));
		assertArrayEquals(new String[] { ",", "1" }, SymbolConfig.Symbol.COMMA.split(",1", SymbolConfig.FullType.PREFIX));
		assertArrayEquals(new String[] { "1,", "" }, SymbolConfig.Symbol.COMMA.split("1,", SymbolConfig.FullType.PREFIX));
		assertArrayEquals(new String[] { "1,", "2" }, SymbolConfig.Symbol.COMMA.split("1,2", SymbolConfig.FullType.PREFIX));
		assertArrayEquals(new String[] { "1,", ",", "2" }, SymbolConfig.Symbol.COMMA.split("1,,2", SymbolConfig.FullType.PREFIX));
		assertArrayEquals(new String[] { "1,", " ,", "2" }, SymbolConfig.Symbol.COMMA.split("1, ,2", SymbolConfig.FullType.PREFIX));
		assertArrayEquals(new String[] { "1" }, SymbolConfig.Symbol.COMMA.split("1", SymbolConfig.FullType.SUFFIX));
		assertArrayEquals(new String[] { "", ",1" }, SymbolConfig.Symbol.COMMA.split(",1", SymbolConfig.FullType.SUFFIX));
		assertArrayEquals(new String[] { "1", "," }, SymbolConfig.Symbol.COMMA.split("1,", SymbolConfig.FullType.SUFFIX));
		assertArrayEquals(new String[] { "1", ",2" }, SymbolConfig.Symbol.COMMA.split("1,2", SymbolConfig.FullType.SUFFIX));
		assertArrayEquals(new String[] { "1", ",", ",2" }, SymbolConfig.Symbol.COMMA.split("1,,2", SymbolConfig.FullType.SUFFIX));
		assertArrayEquals(new String[] { "1", ", ", ",2" }, SymbolConfig.Symbol.COMMA.split("1, ,2", SymbolConfig.FullType.SUFFIX));
	}
	
	@Test
	void testLine() {
		assertNotEquals("", "\r");
		assertNotEquals("", "\n");
		assertNotEquals("", "\r\n");
		assertEquals("", "\r".trim());
		assertEquals("", "\n".trim());
		assertEquals("", "\r\n".trim());
		assertEquals("", "\r".strip());
		assertEquals("", "\n".strip());
		assertEquals("", "\r\n".strip());
	}
	
	@Test
	void testCosted() {
		String key = "key";
		String value = "value";
		this.costed(1000000, () -> {
			final String result = key + ":" + value;
			assertNotNull(result);
		});
		this.costed(1000000, () -> {
			final String result = String.join(":", key, value);
			assertNotNull(result);
		});
		this.costed(1000000, () -> {
			final String result = SymbolConfig.Symbol.COLON.join(key, value);
			assertNotNull(result);
		});
		this.costed(1000000, () -> {
			final String[] result = SymbolConfig.Symbol.COLON.split("1:2:3:4");
			assertNotNull(result);
		});
		this.costed(1000000, () -> {
			final String[] result = "1:2:3:4".split(":");
			assertNotNull(result);
		});
	}
	
}
