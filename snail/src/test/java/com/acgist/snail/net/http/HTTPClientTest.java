package com.acgist.snail.net.http;

import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;

import com.acgist.snail.BaseTest;
import com.acgist.snail.context.exception.NetException;

public class HTTPClientTest extends BaseTest {
	
	@Test
	public void testChar() throws Exception {
		// 错误代码：￨ﾜﾗ￧ﾉﾛ.txt
		var name = "蜗牛.txt";
		this.read(new String(new String(name.getBytes(), "ISO-8859-1").getBytes()));
	}
	
	private void read(String name) throws Exception {
		this.log(name);
		var bytes = name.getBytes("ISO-8859-1");
		var chars = name.toCharArray();
		var chare = new char[bytes.length];
		for (int i = 0; i < chars.length; i++) {
//			this.log(bytes[i] + "=" + ((char) (bytes[i])) + "=" + ((char) (0xFF & bytes[i])));
			// 如果不做0xFF操作异常
//			chare[i] = (char) (bytes[i] & 0xFF); // 正常
			chare[i] = (char) (bytes[i]); // 异常
		}
		this.log(new String(chare));
		this.log(bytes.length);
		this.log(bytes);
		this.log(chars.length);
		this.log(chars);
		this.log(chars[0] & 0xFF);
	}

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
//		var header = HTTPClient.newInstance("https://g18.gdl.netease.com/MY-1.246.1.apk").head();
		var header = HTTPClient.newInstance("http://share.qiniu.easepan.xyz/tool/7tt_setup.exe").head();
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
