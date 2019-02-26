package com.acgist.snail.module.coder.thunder;

import java.util.Base64;

import com.acgist.snail.utils.StringUtils;

/**
 * 迅雷链接解码
 */
public class ThunderDecoder {

	private static final String THUNDER_PREFIX = "thunder://";

	/**
	 * 验证
	 */
	public static final boolean verify(String url) {
		return StringUtils.startsWith(url, THUNDER_PREFIX);
	}
	
	/**
	 * 解码
	 */
	public static final String decode(String url) {
		url = url.substring(THUNDER_PREFIX.length());
		String newUrl = new String(Base64.getDecoder().decode(url));
		return newUrl.substring(2, newUrl.length() - 2);
	}

}
