package com.acgist.snail.net.http;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.http.HttpResponse.BodyHandlers;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.utils.Performance;

public class HTTPClientTest extends Performance {
	
	@Test
	public void testRequest() throws NetException {
		var client = HTTPClient.newInstance("https://gitee.com");
		var response = client.get(BodyHandlers.ofString());
		assertNotNull(response.body());
//		client = HTTPClient.newInstance("https://github.com");
//		response = client.get(BodyHandlers.ofString());
//		assertNotNull(response.body());
		client = HTTPClient.newInstance("http://www.acgist.com");
		response = client.get(BodyHandlers.ofString());
		assertNotNull(response.body());
		client = HTTPClient.newInstance("https://www.acgist.com");
		response = client.get(BodyHandlers.ofString());
		assertNotNull(response.body());
		client = HTTPClient.newInstance("https://www.baidu.com");
		response = client.get(BodyHandlers.ofString());
		assertNotNull(response.body());
		client = HTTPClient.newInstance("https://www.aliyun.com");
		response = client.get(BodyHandlers.ofString());
		assertNotNull(response.body());
		client = HTTPClient.newInstance("http://favor.fund.eastmoney.com/");
		response = client.get(BodyHandlers.ofString());
		assertNotNull(response.body());
	}
	
	@Test
	public void testHttps() throws NetException {
//		var client = HTTPClient.newInstance("https://gitee.com/");
//		var client = HTTPClient.newInstance("https://yys.163.com/");
		var client = HTTPClient.newInstance("https://www.acgist.com");
		var response = client.get(BodyHandlers.ofString());
		var ssl = response.sslSession().get();
		this.log(ssl.getCipherSuite());
		this.log(ssl.getProtocol());
		assertNotNull(response.body());
	}
	
	@Test
	public void testCosted() {
		final long costed = this.costed(100, 10, () -> {
			try {
				HTTPClient.get("https://www.acgist.com", BodyHandlers.ofString());
			} catch (NetException e) {
				LOGGER.error("HTTP请求异常", e);
			}
		});
		assertTrue(costed < 4000);
	}
	
}
