package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.UnsupportedEncodingException;

import org.junit.jupiter.api.Test;

class StringUtilsTest extends Performance {

	@Test
	void testEmpty() {
		assertTrue(StringUtils.isEmpty(null));
		assertTrue(StringUtils.isEmpty(""));
		assertFalse(StringUtils.isEmpty("1"));
		assertFalse(StringUtils.isNotEmpty(null));
		assertFalse(StringUtils.isNotEmpty(""));
		assertTrue(StringUtils.isNotEmpty("1"));
	}
	
	@Test
	void testNumber() {
		assertFalse(StringUtils.isNumeric(""));
		assertTrue(StringUtils.isNumeric("1"));
		assertFalse(StringUtils.isNumeric("1.1"));
		assertFalse(StringUtils.isNumeric("acgist"));
		assertFalse(StringUtils.isDecimal(""));
		assertTrue(StringUtils.isDecimal("1"));
		assertTrue(StringUtils.isDecimal("1.1"));
		assertFalse(StringUtils.isDecimal("acgist"));
	}

	@Test
	void testStartEnd() {
		assertTrue(StringUtils.startsWith("1234", "12"));
		assertFalse(StringUtils.startsWith("1234", "012"));
		assertTrue(StringUtils.endsWith("1234", "34"));
		assertFalse(StringUtils.endsWith("1234", "345"));
	}

	@Test
	void testHex() {
		final String value = "测试";
		final String hex = StringUtils.hex(value.getBytes());
		assertEquals(value, new String(StringUtils.unhex(hex)));
	}

	@Test
	void testSha1() {
		assertEquals("69345456767060fe5970bec185c718f777348af1", StringUtils.sha1Hex("12341234123412341234".getBytes()));
	}

	@Test
	void testCharset() {
		final String source = "对啊这就是一个测试啊逗比";
		final String target = "瀵瑰晩杩欏氨鏄竴涓祴璇曞晩閫楁瘮";
		String value = StringUtils.charsetFrom(target, "gbk");
		assertEquals(source, value);
		value = StringUtils.charsetTo(source, "gbk");
		assertEquals(target, value);
	}

	@Test
	void testUnicode() {
		final String source = "测试代码";
		final String target = "\\u6d4b\\u8bd5\\u4ee3\\u7801";
		assertEquals(target, StringUtils.toUnicode("测试代码"));
		assertEquals(source, StringUtils.ofUnicode(target));
		assertEquals("\\u0002", StringUtils.toUnicode(Character.toString(2)));
		this.costed(100000, () -> StringUtils.toUnicode("测试代码"));
	}
	
	@Test
	void testArgValue() {
		assertEquals(null, StringUtils.argValue("modeextend", "mode"));
		assertEquals("extend", StringUtils.argValue("mode=extend", "mode"));
	}

	@Test
	void testGetCharset() throws UnsupportedEncodingException {
		assertEquals("GBK", StringUtils.getCharset(new String("测试".getBytes("GBK"))));
		assertEquals("UTF-8", StringUtils.getCharset("测试"));
	}

	@Test
	void testTrimBlank() {
		final String source = " 0 0 0 	\n \r ";
		final String target = StringUtils.trimAllBlank(source);
		assertEquals("000", target);
	}

	@Test
	void testReadLines() {
		final var lines = StringUtils.readLines("1\n2\n\r");
		assertEquals("1", lines.get(0));
		assertEquals("2", lines.get(1));
		assertEquals(2, lines.size());
	}
	
	@Test
	void testStartsWithIgnoreCase() {
		assertFalse(StringUtils.startsWithIgnoreCase(null, null));
		assertFalse(StringUtils.startsWithIgnoreCase("", null));
		assertFalse(StringUtils.startsWithIgnoreCase(null, ""));
		assertTrue(StringUtils.startsWithIgnoreCase("", ""));
		assertTrue(StringUtils.startsWithIgnoreCase("1", ""));
		assertTrue(StringUtils.startsWithIgnoreCase("1", "1"));
		assertFalse(StringUtils.startsWithIgnoreCase("", "1"));
		assertTrue(StringUtils.startsWithIgnoreCase("/INDEX", "/index"));
		assertTrue(StringUtils.startsWithIgnoreCase("INDEX/", "index"));
		assertFalse(StringUtils.startsWithIgnoreCase("INDEX", "/index"));
	}
	
	@Test
	void testEndsWithIgnoreCase() {
		assertFalse(StringUtils.endsWithIgnoreCase(null, null));
		assertFalse(StringUtils.endsWithIgnoreCase("", null));
		assertFalse(StringUtils.endsWithIgnoreCase(null, ""));
		assertTrue(StringUtils.endsWithIgnoreCase("", ""));
		assertTrue(StringUtils.endsWithIgnoreCase("1", ""));
		assertTrue(StringUtils.endsWithIgnoreCase("1", "1"));
		assertFalse(StringUtils.endsWithIgnoreCase("", "1"));
		assertTrue(StringUtils.endsWithIgnoreCase("/INDEX", "/index"));
		assertTrue(StringUtils.endsWithIgnoreCase("/INDEX", "index"));
		assertFalse(StringUtils.endsWithIgnoreCase("INDEX", "/index"));
	}
	
}
