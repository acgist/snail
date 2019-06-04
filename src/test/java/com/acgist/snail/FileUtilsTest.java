package com.acgist.snail;

import org.junit.Test;

import com.acgist.snail.net.http.HTTPClient;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.FileUtils;

public class FileUtilsTest {

	@Test
	public void fileNameUrl() {
		System.out.println(FileUtils.fileNameFromUrl("http://casd/%e8%ae%a2%e5%8d%95fds.mpe?xx"));
		System.out.println(FileUtils.fileNameFromUrl("https://www.acgist.com/demo/weixin/view?xx?xx"));
	}
	
	@Test
	public void fileNameHttp() throws NetException {
		HTTPClient.head("https://www.acgist.com/demo/weixin/view").headers().forEach((key, value) -> {
			System.out.println(key + "-" + value);
		});
	}
	
	@Test
	public void fileType() {
		System.out.println(FileUtils.fileType("http://casd/fds.mpe"));
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
	
}
