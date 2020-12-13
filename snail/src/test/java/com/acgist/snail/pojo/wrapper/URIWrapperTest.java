package com.acgist.snail.pojo.wrapper;

import java.net.URI;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

public class URIWrapperTest extends Performance {
	
	@Test
	public void testURI() {
		final URI uri = URI.create("https://user:password@www.acgist.com:8080/index.html?a=a#b");
		this.log(uri.getScheme());
		this.log(uri.getUserInfo());
		this.log(uri.getAuthority());
		this.log(uri.getHost());
		this.log(uri.getPort());
		this.log(uri.getPath());
		this.log(uri.getQuery());
		this.log(uri.getFragment());
		this.log(uri.getSchemeSpecificPart());
		this.log(uri.isAbsolute());
		this.log(URI.create("/index.html").isAbsolute());
	}

	@Test
	public void testDecode() {
		URIWrapper wrapper = URIWrapper.newInstance("ftp://localhost/path?query=query").decode();
		this.log("{}-{}-{}-{}-{}-{}", wrapper.host(), wrapper.port(), wrapper.user(), wrapper.password(), wrapper.path(), wrapper.query());
		wrapper = URIWrapper.newInstance("ftp://localhost/path?query=query", 21).decode();
		this.log("{}-{}-{}-{}-{}-{}", wrapper.host(), wrapper.port(), wrapper.user(), wrapper.password(), wrapper.path(), wrapper.query());
		wrapper = URIWrapper.newInstance("ftp://localhost/path?query=query", 21, "user", "password").decode();
		this.log("{}-{}-{}-{}-{}-{}", wrapper.host(), wrapper.port(), wrapper.user(), wrapper.password(), wrapper.path(), wrapper.query());
		wrapper = URIWrapper.newInstance("ftp://localhost:8080/path?query=query").decode();
		this.log("{}-{}-{}-{}-{}-{}", wrapper.host(), wrapper.port(), wrapper.user(), wrapper.password(), wrapper.path(), wrapper.query());
		wrapper = URIWrapper.newInstance("ftp://user@localhost:8080/path?query=query").decode();
		this.log("{}-{}-{}-{}-{}-{}", wrapper.host(), wrapper.port(), wrapper.user(), wrapper.password(), wrapper.path(), wrapper.query());
		wrapper = URIWrapper.newInstance("ftp://user:password@localhost:8080/path?query=query").decode();
		this.log("{}-{}-{}-{}-{}-{}", wrapper.host(), wrapper.port(), wrapper.user(), wrapper.password(), wrapper.path(), wrapper.query());
	}
	
}
