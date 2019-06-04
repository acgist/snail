package com.acgist.snail.http;

import java.net.http.HttpResponse.BodyHandlers;

import org.junit.Test;

import com.acgist.snail.net.http.HTTPClient;
import com.acgist.snail.system.exception.NetException;

public class HTTPClientTest {

	@Test
	public void test() throws NetException {
//		var response = HTTPClient.get("http://www.acgist.com", BodyHandlers.ofString());
//		System.out.println(response.body());
		var response = HTTPClient.post("http://www.acgist.com", null, BodyHandlers.ofString());
		System.out.println(response.body());
	}
	
	@Test
	public void fileName() throws NetException {
		var header = HTTPClient.head("https://g37.gdl.netease.com/onmyoji_setup_9.4.0.zip");
		System.out.println(header.fileName("test"));
		System.out.println(header);
	}
	
}
