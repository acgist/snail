package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

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
	
	@Test
	public void encode() throws UnsupportedEncodingException {
		// 空格编码变成加号：加号解码变成空格
		System.out.println(URLEncoder.encode("+ +", "UTF-8"));
		System.out.println(URLDecoder.decode("%2B+%2B", "UTF-8"));
		System.out.println(UrlUtils.encode("+ +"));
		System.out.println(UrlUtils.decode("%2B%20%2B"));
	}
	
}
