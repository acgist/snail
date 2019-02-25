package com.acgist.snail.module.decoder;

import java.util.Base64;

import org.apache.commons.lang3.StringUtils;

/**
 * 迅雷链接解码
 */
public class ThunderDecoder {

	private static final String THUNDER_HEADER = "thunder://";

	/**
	 * 验证
	 */
	public static final boolean verify(String url) {
		return StringUtils.startsWith(url, THUNDER_HEADER);
	}
	
	/**
	 * 解码
	 */
	public static final String decode(String url) {
		url = url.substring(THUNDER_HEADER.length());
		String newUrl = new String(Base64.getDecoder().decode(url));
		return newUrl.substring(2, newUrl.length() - 2);
	}
	
}
