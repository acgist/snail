package com.acgist.snail.net.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.NetException;
import com.acgist.snail.utils.Performance;

class HttpClientTest extends Performance {

	@Test
	void testCode() throws NetException {
		var client = HttpClient.newInstance("https://gitee.com").get();
//		var client = HttpClient.newInstance("http://localhost:8080/timeout", 5000, 5000).get();
		assertEquals(200, client.code());
		this.log(client.code());
		this.log(client.responseToString());
	}
	
	@Test
	void testRequest() throws NetException {
		var client = HttpClient.newInstance("https://gitee.com");
		assertNotNull(client.get());
		this.log(client.responseToString());
		client = HttpClient.newInstance("http://www.acgist.com");
		assertNotNull(client.get());
		this.log(client.responseToString());
		client = HttpClient.newInstance("https://www.acgist.com");
		assertNotNull(client.get());
		this.log(client.responseToString());
		client = HttpClient.newInstance("https://www.baidu.com");
		assertNotNull(client.get());
		this.log(client.responseToString());
		client = HttpClient.newInstance("https://www.aliyun.com");
		assertNotNull(client.get());
		this.log(client.responseToString());
		client = HttpClient.newInstance("http://favor.fund.eastmoney.com/");
		assertNotNull(client.get());
		this.log(client.responseToString());
	}
	
	@Test
	void testCosted() {
		final long costed = this.costed(100, 10, () -> {
			try {
				HttpClient.newInstance("https://www.acgist.com").get();
			} catch (NetException e) {
				LOGGER.error("HTTP请求异常", e);
			}
		});
		assertTrue(costed < 4000);
	}

	@Test
	void testUrlCosted() {
		String url = "https://www.acgist.com";
		long costed = this.costed(100000, () -> {
			try {
				URI.create(url).toURL();
			} catch (MalformedURLException e) {
				this.log("新建URL异常", e);
			}
		});
		assertTrue(costed < 1000);
		costed = this.costed(100000, () -> {
			try {
				new URL(url);
			} catch (MalformedURLException e) {
				this.log("新建URL异常", e);
			}
		});
		assertTrue(costed < 1000);
	}
	
}
