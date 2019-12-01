package com.acgist.snail;

import java.net.http.HttpResponse.BodyHandlers;

import org.junit.Test;

import com.acgist.snail.net.http.HTTPClient;
import com.acgist.snail.system.exception.NetException;

public class HTTPClientTest extends BaseTest {

	@Test
	public void request() throws NetException {
		HTTPClient client = HTTPClient.newInstance("http://www.acgist.com");
		var response = client.get(BodyHandlers.ofString());
//		var response = client.post("test", BodyHandlers.ofString());
//		var response = client.post(null, BodyHandlers.ofString());
		this.log(response.body());
	}
	
	@Test
	public void fileName() throws NetException {
		var header = HTTPClient.newInstance("https://g18.gdl.netease.com/MY-1.246.1.apk").head();
//		var header = HTTPClient.newInstance("https://g37.gdl.netease.com/onmyoji_setup_9.4.0.zip").head();
		this.log(header.fileName("test"));
		this.log(header);
	}
	
}
