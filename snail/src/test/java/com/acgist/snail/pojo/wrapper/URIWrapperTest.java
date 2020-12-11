package com.acgist.snail.pojo.wrapper;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

public class URIWrapperTest extends Performance {

	@Test
	public void testDecode() {
		URIWrapper wrapper = URIWrapper.newInstance("ftp://localhost/path?query=query");
		wrapper.decode();
		this.log("{}-{}-{}-{}-{}-{}", wrapper.host(), wrapper.port(), wrapper.user(), wrapper.password(), wrapper.path(), wrapper.query());
		wrapper = URIWrapper.newInstance("ftp://localhost/path?query=query", 21);
		wrapper.decode();
		this.log("{}-{}-{}-{}-{}-{}", wrapper.host(), wrapper.port(), wrapper.user(), wrapper.password(), wrapper.path(), wrapper.query());
		wrapper = URIWrapper.newInstance("ftp://localhost/path?query=query", 21, "user", "password");
		wrapper.decode();
		this.log("{}-{}-{}-{}-{}-{}", wrapper.host(), wrapper.port(), wrapper.user(), wrapper.password(), wrapper.path(), wrapper.query());
		wrapper = URIWrapper.newInstance("ftp://localhost:8080/path?query=query");
		wrapper.decode();
		this.log("{}-{}-{}-{}-{}-{}", wrapper.host(), wrapper.port(), wrapper.user(), wrapper.password(), wrapper.path(), wrapper.query());
		wrapper = URIWrapper.newInstance("ftp://user@localhost:8080/path?query=query");
		wrapper.decode();
		this.log("{}-{}-{}-{}-{}-{}", wrapper.host(), wrapper.port(), wrapper.user(), wrapper.password(), wrapper.path(), wrapper.query());
		wrapper = URIWrapper.newInstance("ftp://user:password@localhost:8080/path?query=query");
		wrapper.decode();
		this.log("{}-{}-{}-{}-{}-{}", wrapper.host(), wrapper.port(), wrapper.user(), wrapper.password(), wrapper.path(), wrapper.query());
	}
	
}
