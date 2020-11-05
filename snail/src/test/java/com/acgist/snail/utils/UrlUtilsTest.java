package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.acgist.snail.BaseTest;

public class UrlUtilsTest extends BaseTest {

	@Test
	public void testRedirect() {
		String path = "https://www.acgist.com/index.html";
		String pathA = "https://www.acgist.com/a/index.html";
		String pathAB = "https://www.acgist.com/a/b/index.html";
		assertEquals(UrlUtils.redirect("https://www.acgist.com", "index.html"), path);
		assertEquals(UrlUtils.redirect("https://www.acgist.com/", "index.html"), path);
		assertEquals(UrlUtils.redirect("https://www.acgist.com", "/index.html"), path);
		assertEquals(UrlUtils.redirect("https://www.acgist.com/", "/index.html"), path);
		assertEquals(UrlUtils.redirect("https://www.acgist.com/a", "index.html"), path);
		assertEquals(UrlUtils.redirect("https://www.acgist.com/a/", "index.html"), pathA);
		assertEquals(UrlUtils.redirect("https://www.acgist.com/a", "/index.html"), path);
		assertEquals(UrlUtils.redirect("https://www.acgist.com/a/", "/index.html"), path);
		assertEquals(UrlUtils.redirect("https://www.acgist.com/a/b", "index.html"), pathA);
		assertEquals(UrlUtils.redirect("https://www.acgist.com/a/b/", "index.html"), pathAB);
		assertEquals(UrlUtils.redirect("https://www.acgist.com/a/b", "/index.html"), path);
		assertEquals(UrlUtils.redirect("https://www.acgist.com/a/b/", "/index.html"), path);
		assertEquals(UrlUtils.redirect("https://www.acgist.com/a/b/", " /index.html "), path);
		assertEquals(UrlUtils.redirect("https://www.acgist.com/a/b/", "\"/index.html\""), path);
		assertEquals(UrlUtils.redirect("https://www.acgist.com/a/b/", " \"/index.html\" "), path);
	}
	
}
