package com.acgist.snail.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.module.config.SystemConfig;

/**
 * URL操作
 */
public class UrlUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(UrlUtils.class);
	
	/**
	 * 编码
	 */
	public static final String encode(String url) {
		try {
			return URLEncoder.encode(url, SystemConfig.DEFAULT_CHARSET);
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("URL编码异常：{}", url, e);
		}
		return url;
	}

	/**
	 * 解码
	 */
	public static final String decode(String url) {
		try {
			return URLDecoder.decode(url, SystemConfig.DEFAULT_CHARSET);
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("URL解码异常：{}", url, e);
		}
		return url;
	}
	
}
