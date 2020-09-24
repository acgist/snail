package com.acgist.snail;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.UnsupportedEncodingException;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.net.http.HTTPClient;
import com.acgist.snail.pojo.wrapper.HttpHeaderWrapper;

public class HTTPClientTest extends BaseTest {
	
	@Test
	public void testChar() throws Exception {
		// 错误代码：￨ﾜﾗ￧ﾉﾛ.txt
		var name = "蜗牛.txt";
		read(new String(new String(name.getBytes(), "ISO-8859-1").getBytes()));
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
	public void mockFileName() throws UnsupportedEncodingException {
		var headers = HttpHeaders.of(Map.of("Content-Disposition", List.of("attachment;filename='snail.jar'")), (a, b) -> true);
		HttpHeaderWrapper headerWrapper = HttpHeaderWrapper.newInstance(headers);
		String filename = headerWrapper.fileName("错误");
		this.log(filename);
		assertEquals(filename, "snail.jar");
		headers = HttpHeaders.of(Map.of("Content-Disposition", List.of("attachment;filename='%e6%b5%8b%e8%af%95.exe'")), (a, b) -> true);
		headerWrapper = HttpHeaderWrapper.newInstance(headers);
		filename = headerWrapper.fileName("错误");
		this.log(filename);
		assertEquals(filename, "测试.exe");
		headers = HttpHeaders.of(Map.of("Content-Disposition", List.of("attachment;filename='" + new String("测试.exe".getBytes("GBK"), "ISO-8859-1") + "'")), (a, b) -> true);
		headerWrapper = HttpHeaderWrapper.newInstance(headers);
		filename = headerWrapper.fileName("错误");
		this.log(filename);
		assertEquals(filename, "测试.exe");
		headers = HttpHeaders.of(Map.of("Content-Disposition", List.of("attachment;filename=\"snail.jar\"")), (a, b) -> true);
		headerWrapper = HttpHeaderWrapper.newInstance(headers);
		filename = headerWrapper.fileName("错误");
		this.log(filename);
		assertEquals(filename, "snail.jar");
		headers = HttpHeaders.of(Map.of("Content-Disposition", List.of("attachment;filename=snail.jar?version=1.0.0")), (a, b) -> true);
		headerWrapper = HttpHeaderWrapper.newInstance(headers);
		filename = headerWrapper.fileName("错误");
		this.log(filename);
		assertEquals(filename, "snail.jar");
		headers = HttpHeaders.of(Map.of("Content-Disposition", List.of("inline;filename=\"snail.jar\";filename*=utf-8''snail.jar")), (a, b) -> true);
		headerWrapper = HttpHeaderWrapper.newInstance(headers);
		filename = headerWrapper.fileName("错误");
		this.log(filename);
		assertEquals(filename, "snail.jar");
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
