package com.acgist.snail;

import org.junit.Test;

import com.acgist.snail.utils.FileVerifyUtils;

public class FileVerifyUtilsTest {

	@Test
	public void verify() {
		FileVerifyUtils.md5("F:\\迅雷下载\\我的大叔\\[我的大叔][E008].mkv").forEach((key, value) -> {
			System.out.println(value + "=" + key);
		});
		FileVerifyUtils.sha1("F:\\迅雷下载\\我的大叔\\[我的大叔][E008].mkv").forEach((key, value) -> {
			System.out.println(value + "=" + key);
		});
	}
	
}
