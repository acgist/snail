package com.acgist.snail.protocol.thunder;

import java.util.Base64;

import com.acgist.snail.utils.StringUtils;

/**
 * 迅雷链接解析器
 */
public class ThunderProtocol {

	public static final String THUNDER_PREFIX = "thunder://";

	/**
	 * 验证
	 */
	public static final boolean verify(String url) {
		return StringUtils.startsWith(url.toLowerCase(), THUNDER_PREFIX);
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
