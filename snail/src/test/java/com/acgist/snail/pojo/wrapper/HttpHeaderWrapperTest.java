package com.acgist.snail.pojo.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.UnsupportedEncodingException;
import java.net.http.HttpHeaders;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.acgist.snail.BaseTest;

public class HttpHeaderWrapperTest extends BaseTest {

	@Test
	public void fileName() throws UnsupportedEncodingException {
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
	
}
