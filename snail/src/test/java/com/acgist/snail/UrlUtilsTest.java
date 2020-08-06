package com.acgist.snail;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.UrlUtils;

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
