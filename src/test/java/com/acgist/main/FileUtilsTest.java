package com.acgist.main;

import org.junit.Test;

import com.acgist.snail.utils.FileUtils;

public class FileUtilsTest {

	@Test
	public void fileNameUrl() {
		System.out.println(FileUtils.fileNameFromUrl("http://casd/%e8%ae%a2%e5%8d%95fds.mpe?xx"));
		System.out.println(FileUtils.fileNameFromUrl("https://www.acgist.com/demo/weixin/view?xx?xx"));
	}
	
	@Test
	public void fileNameHttp() {
		System.out.println(FileUtils.fileNameFromHttp("https://www.acgist.com/demo/weixin/view"));
	}
	
	@Test
	public void fileType() {
		System.out.println(FileUtils.fileType("http://casd/fds.mpe"));
	}
	
}
