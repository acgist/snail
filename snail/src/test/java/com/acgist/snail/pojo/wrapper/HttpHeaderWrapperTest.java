package com.acgist.snail.pojo.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.net.http.HttpClient;
import com.acgist.snail.utils.Performance;

public class HttpHeaderWrapperTest extends Performance {

	@Test
	public void testFileName() throws NetException {
		final var header = HttpClient.newInstance("https://g18.gdl.netease.com/MY-1.246.1.apk").head().responseHeader();
//		final var header = HttpClient.newInstance("http://share.qiniu.easepan.xyz/tool/7tt_setup.exe").head().responseHeader();
//		final var header = HttpClient.newInstance("https://g37.gdl.netease.com/onmyoji_setup_9.4.0.zip").head().responseHeader();
		this.log(header);
		final String defaultName = "test";
		final String fileName = header.fileName(defaultName);
		this.log(fileName);
		assertNotEquals(defaultName, fileName);
	}
	
	@Test
	public void testFileNameEx() throws UnsupportedEncodingException {
		var headers = Map.of("Content-Disposition", List.of("attachment;filename='snail.jar'"));
		var headerWrapper = HttpHeaderWrapper.newInstance(headers);
		var fileName = headerWrapper.fileName("错误");
		this.log(fileName);
		assertEquals("snail.jar", fileName);
		headers = Map.of("Content-Disposition", List.of("attachment;filename='%e6%b5%8b%e8%af%95.exe'"));
		headerWrapper = HttpHeaderWrapper.newInstance(headers);
		fileName = headerWrapper.fileName("错误");
		this.log(fileName);
		assertEquals("测试.exe", fileName);
		headers = Map.of("Content-Disposition", List.of("attachment;filename='" + new String("测试.exe".getBytes("GBK"), "ISO-8859-1") + "'"));
		headerWrapper = HttpHeaderWrapper.newInstance(headers);
		fileName = headerWrapper.fileName("错误");
		this.log(fileName);
		assertEquals("测试.exe", fileName);
		headers = Map.of("Content-Disposition", List.of("attachment;filename=\"snail.jar\""));
		headerWrapper = HttpHeaderWrapper.newInstance(headers);
		fileName = headerWrapper.fileName("错误");
		this.log(fileName);
		assertEquals("snail.jar", fileName);
		headers = Map.of("Content-Disposition", List.of("attachment;filename=\"￨ﾜﾗ￧ﾉﾛ.txt\""));
		headerWrapper = HttpHeaderWrapper.newInstance(headers);
		fileName = headerWrapper.fileName("错误");
		this.log(fileName);
		assertEquals("蜗牛.txt", fileName);
		headers = Map.of("Content-Disposition", List.of("attachment;filename=snail.jar?version=1.0.0"));
		headerWrapper = HttpHeaderWrapper.newInstance(headers);
		fileName = headerWrapper.fileName("错误");
		this.log(fileName);
		assertEquals("snail.jar", fileName);
		headers = Map.of("Content-Disposition", List.of("inline;filename=\"snail.jar\";filename*=utf-8''snail.jar"));
		headerWrapper = HttpHeaderWrapper.newInstance(headers);
		fileName = headerWrapper.fileName("错误");
		this.log(fileName);
		assertEquals("snail.jar", fileName);
		headers = Map.of("Content-Disposition", List.of("inline;charset=utf-8;fileName=\"snail.jar\";filename*=utf-8''snail.jar"));
		headerWrapper = HttpHeaderWrapper.newInstance(headers);
		fileName = headerWrapper.fileName("错误");
		this.log(fileName);
		assertEquals("snail.jar", fileName);
	}
	
}
