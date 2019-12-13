package com.acgist.snail;

import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import org.junit.Test;

import com.acgist.snail.net.http.HTTPClient;
import com.acgist.snail.system.exception.NetException;

public class HTTPClientTest extends BaseTest {

	@Test
	public void testRequest() throws NetException {
		HTTPClient client = HTTPClient.newInstance("http://www.acgist.com");
		var response = client.get(BodyHandlers.ofString());
//		var response = client.post("test", BodyHandlers.ofString());
//		var response = client.post(null, BodyHandlers.ofString());
		this.log(response.body());
	}
	
	@Test
	public void testFileName() throws NetException {
		var header = HTTPClient.newInstance("https://g18.gdl.netease.com/MY-1.246.1.apk").head();
//		var header = HTTPClient.newInstance("https://g37.gdl.netease.com/onmyoji_setup_9.4.0.zip").head();
		this.log(header);
		this.log("文件名称：" + header.fileName("test"));
	}
	
	@Test
	public void testThread() throws Exception {
		int count = 10;
		var executor = Executors.newFixedThreadPool(count);
		var downLatch = new CountDownLatch(count);
		this.cost();
		for (int index = 0; index < 20; index++) {
			executor.submit(() -> {
				try {
					var body = HTTPClient.get("https://www.acgist.com", BodyHandlers.ofString());
					this.log(body);
				} catch (NetException e) {
					this.log(e);
				} finally {
					downLatch.countDown();
				}
			});
		}
		downLatch.await();
		this.costed();
	}
	
}
