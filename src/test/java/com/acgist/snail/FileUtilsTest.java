package com.acgist.snail;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.Test;

import com.acgist.snail.net.http.HTTPClient;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.StringUtils;

public class FileUtilsTest {

	@Test
	public void fileNameUrl() {
		System.out.println(FileUtils.fileNameFromUrl("http://casd/%e8%ae%a2%e5%8d%95fds.mpe?xx"));
		System.out.println(FileUtils.fileNameFromUrl("https://www.acgist.com/demo/weixin/view?xx?xx"));
	}
	
	@Test
	public void fileNameHttp() throws NetException {
		HTTPClient client = HTTPClient.newInstance("https://www.acgist.com/demo/weixin/view");
		var headers = client.head();
		headers.allHeaders().forEach((key, value) -> {
			System.out.println(key + "<---->" + value);
		});
		System.out.println(headers.header("SERVER"));
	}
	
	@Test
	public void fileType() {
		System.out.println(FileUtils.fileType("http://casd/fds.mp4"));
	}
	
	@Test
	public void size() {
		System.out.println(FileUtils.fileSize("E:\\gitee\\snail\\download\\MY-1.211.0.exe"));
	}
	
	@Test
	public void verify() {
		FileUtils.md5("F:\\迅雷下载\\我的大叔\\[我的大叔][E008].mkv").forEach((key, value) -> {
			System.out.println(value + "=" + key);
		});
		FileUtils.sha1("F:\\迅雷下载\\我的大叔\\[我的大叔][E008].mkv").forEach((key, value) -> {
			System.out.println(value + "=" + key);
		});
	}
	
	@Test
	public void order() {
		ByteBuffer buffer = ByteBuffer.allocate(2);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.putChar('你');
		System.out.println(StringUtils.hex(buffer.array()));
	}
	
}
