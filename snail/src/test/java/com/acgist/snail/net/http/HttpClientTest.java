package com.acgist.snail.net.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.utils.Performance;

public class HttpClientTest extends Performance {

	@Test
	public void testCode() throws NetException {
		var client = HttpClient.newInstance("http://localhost:8080/timeout", 5000, 5000).get();
//		var client = HttpClient.newInstance("https://gitee.com").get();
		assertEquals(200, client.code());
		this.log(client.code());
		this.log(client.responseToString());
	}
	
	@Test
	public void testRequest() throws NetException {
		var client = HttpClient.newInstance("https://gitee.com");
		assertNotNull(client.get().responseToString());
		client = HttpClient.newInstance("http://www.acgist.com");
		assertNotNull(client.get().responseToString());
		client = HttpClient.newInstance("https://www.acgist.com");
		assertNotNull(client.get().responseToString());
		client = HttpClient.newInstance("https://www.baidu.com");
		assertNotNull(client.get().responseToString());
		client = HttpClient.newInstance("https://www.aliyun.com");
		assertNotNull(client.get().responseToString());
		client = HttpClient.newInstance("http://favor.fund.eastmoney.com/");
		assertNotNull(client.get().responseToString());
	}
	
	@Test
	public void testCosted() {
		final long costed = this.costed(100, 10, () -> {
			try {
				HttpClient.newInstance("https://www.acgist.com").get();
			} catch (NetException e) {
				LOGGER.error("HTTP请求异常", e);
			}
		});
		assertTrue(costed < 4000);
	}
	
}
