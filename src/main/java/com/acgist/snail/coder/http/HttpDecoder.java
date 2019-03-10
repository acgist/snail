package com.acgist.snail.coder.http;

import com.acgist.snail.utils.StringUtils;

/**
 * HTTP解析器
 */
public class HttpDecoder {

	public static final String HTTP_REGEX = "http://.+";
	public static final String HTTPS_REGEX = "https://.+";
	public static final String HTTP_PREFIX = "http://";
	public static final String HTTPS_PREFIX = "https://";
	
	/**
	 * 验证HTTP
	 */
	public static final boolean verify(final String url) {
		return verifyHttp(url) || verifyHttps(url);
	}
	
	/**
	 * 验证HTTP
	 */
	public static final boolean verifyHttp(final String url) {
		return StringUtils.startsWith(url.toLowerCase(), HTTP_PREFIX);
	}
	
	/**
	 * 验证HTTPS
	 */
	public static final boolean verifyHttps(final String url) {
		return StringUtils.startsWith(url.toLowerCase(), HTTPS_PREFIX);
	}
	
}
