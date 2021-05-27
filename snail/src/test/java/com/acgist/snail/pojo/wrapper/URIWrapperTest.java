package com.acgist.snail.pojo.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

class URIWrapperTest extends Performance {
	
	@Test
	void testDecode() {
		URIWrapper wrapper = URIWrapper.newInstance("ftp://localhost/path?query=query").decode();
		assertEquals("ftp", wrapper.scheme());
		assertEquals("localhost", wrapper.host());
		assertEquals("/path", wrapper.path());
		assertEquals("query=query", wrapper.query());
		assertNull(wrapper.user());
		assertEquals(0, wrapper.port());
		assertNull(wrapper.password());
		assertNull(wrapper.fragment());
		wrapper = URIWrapper.newInstance("ftp://localhost/path?query=query", 21).decode();
		assertEquals(21, wrapper.port());
		wrapper = URIWrapper.newInstance("ftp://localhost/path?query=query", 21, "user", "password").decode();
		assertEquals("user", wrapper.user());
		assertEquals("password", wrapper.password());
		wrapper = URIWrapper.newInstance("ftp://localhost:8080/path?query=query").decode();
		assertEquals(8080, wrapper.port());
		wrapper = URIWrapper.newInstance("ftp://user@localhost:8080/path?query=query").decode();
		assertEquals("user", wrapper.user());
		assertNull(wrapper.password());
		wrapper = URIWrapper.newInstance("ftp://user:password@localhost:8080/path?query=query#acgist").decode();
		assertEquals("user", wrapper.user());
		assertEquals("password", wrapper.password());
		assertEquals("acgist", wrapper.fragment());
	}
	
}
