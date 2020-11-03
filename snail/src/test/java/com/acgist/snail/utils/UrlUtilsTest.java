package com.acgist.snail.utils;

import org.junit.jupiter.api.Test;

import com.acgist.snail.BaseTest;

public class UrlUtilsTest extends BaseTest {

	@Test
	public void testRedirect() {
		this.log(UrlUtils.redirect("https://www.acgist.com", "index.html"));
		this.log(UrlUtils.redirect("https://www.acgist.com/", "index.html"));
		this.log(UrlUtils.redirect("https://www.acgist.com", "/index.html"));
		this.log(UrlUtils.redirect("https://www.acgist.com/", "/index.html"));
		this.log(UrlUtils.redirect("https://www.acgist.com/a", "index.html"));
		this.log(UrlUtils.redirect("https://www.acgist.com/a/", "index.html"));
		this.log(UrlUtils.redirect("https://www.acgist.com/a", "/index.html"));
		this.log(UrlUtils.redirect("https://www.acgist.com/a/", "/index.html"));
		this.log(UrlUtils.redirect("https://www.acgist.com/a/b", "index.html"));
		this.log(UrlUtils.redirect("https://www.acgist.com/a/b/", "index.html"));
		this.log(UrlUtils.redirect("https://www.acgist.com/a/b", "/index.html"));
		this.log(UrlUtils.redirect("https://www.acgist.com/a/b/", "/index.html"));
	}
	
}
