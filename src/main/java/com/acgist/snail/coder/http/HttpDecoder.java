package com.acgist.snail.coder.http;

import com.acgist.snail.utils.StringUtils;

/**
 * HTTP解析器
 */
public class HttpDecoder {

	private static final String HTTP_PREFIX = "http://";
	private static final String HTTPS_PREFIX = "https://";
	
	public static final boolean verify(String url) {
		return StringUtils.startsWith(url.toLowerCase(), HTTP_PREFIX) ||
			StringUtils.startsWith(url.toLowerCase(), HTTPS_PREFIX);
	}
	
}
