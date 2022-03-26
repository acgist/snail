package com.acgist.snail.pojo.wrapper;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

class URIWrapperTest extends Performance {
	
	@Test
	void testDecode() {
		URIWrapper wrapper = URIWrapper.newInstance("ftp://localhost/path?query=query").decode();
		assertEquals("ftp", wrapper.scheme());
		assertEquals("localhost", wrapper.authority());
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
		assertEquals("localhost", wrapper.host());
		assertEquals("localhost:8080", wrapper.authority());
		wrapper = URIWrapper.newInstance("ftp://user@localhost:8080/path?query=query").decode();
		assertEquals("user", wrapper.user());
		assertNull(wrapper.password());
		wrapper = URIWrapper.newInstance("ftp://user:password@localhost:8080/path?query=query#acgist").decode();
		assertEquals("user", wrapper.user());
		assertEquals("password", wrapper.password());
		assertEquals("acgist", wrapper.fragment());
	}
	
	@Test
	void testQuerys() {
		URIWrapper wrapper = URIWrapper.newInstance("ftp://localhost/path?query=query&name=name&password=%E7%9A%84").decode();
		assertEquals("//localhost/path?query=query&name=name&password=的", wrapper.schemeSpecificPart());
		assertEquals("query=query&name=name&password=的", wrapper.query());
		assertArrayEquals(new String[] {"query=query", "name=name", "password=的"}, wrapper.querys());
		wrapper = URIWrapper.newInstance("ftp:?query=query&name=name&password=%E7%9A%84#fragment").decode();
		assertEquals("?query=query&name=name&password=的", wrapper.schemeSpecificPart());
		assertEquals(null, wrapper.query());
		assertEquals("fragment", wrapper.fragment());
		assertArrayEquals(new String[] {"query=query", "name=name", "password=的"}, wrapper.querys());
		wrapper = URIWrapper.newInstance("magnet:?xt=urn:btih:08ada5a7a6183aae1e09d831df6748d566095a10&dn=Sintel%20%e6%b5%8b%e8%af%95&tr=udp%3A%2F%2Fexplodie.org%3A6969&tr=udp%3A%2F%2Ftracker.coppersurfer.tk%3A6969&tr=udp%3A%2F%2Ftracker.empire-js.us%3A1337&tr=udp%3A%2F%2Ftracker.leechers-paradise.org%3A6969&tr=udp%3A%2F%2Ftracker.opentrackr.org%3A1337&tr=wss%3A%2F%2Ftracker.btorrent.xyz&tr=wss%3A%2F%2Ftracker.fastcast.nz&tr=wss%3A%2F%2Ftracker.openwebtorrent.com&ws=https%3A%2F%2Fwebtorrent.io%2Ftorrents%2F&xs=https%3A%2F%2Fwebtorrent.io%2Ftorrents%2Fsintel.torrent").decode();
		assertNull(wrapper.query());
		for (String value : wrapper.querys()) {
			this.log(value);
		}
		assertThrows(Exception.class, () -> {
			URIWrapper.newInstance("https://www.acgist.com/?name=测 试").decode();
		});
	}
	
}
