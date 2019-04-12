package com.acgist.main;

import java.net.http.HttpResponse.BodyHandlers;

import org.junit.Test;

import com.acgist.snail.net.http.HTTPClient;

public class HTTPClientTest {

	@Test
	public void test() {
//		var response = HTTPClient.get("http://www.acgist.com", BodyHandlers.ofString());
//		System.out.println(response.body());
		var response = HTTPClient.post("http://www.acgist.com", null, BodyHandlers.ofString());
		System.out.println(response.body());
	}
	
}
