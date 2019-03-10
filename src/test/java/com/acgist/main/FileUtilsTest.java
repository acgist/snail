package com.acgist.main;

import org.junit.Test;

import com.acgist.snail.net.http.HttpUtils;
import com.acgist.snail.utils.FileUtils;

public class FileUtilsTest {

	@Test
	public void fileNameUrl() {
		System.out.println(FileUtils.fileNameFromUrl("http://casd/%e8%ae%a2%e5%8d%95fds.mpe?xx"));
		System.out.println(FileUtils.fileNameFromUrl("https://www.acgist.com/demo/weixin/view?xx?xx"));
	}
	
	@Test
	public void fileNameHttp() {
		HttpUtils.httpHeader("https://www.acgist.com/demo/weixin/view").map().forEach((key, value) -> {
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
	
}
