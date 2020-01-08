package com.acgist.snail.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.config.SystemConfig;

/**
 * <p>URL工具</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class UrlUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(UrlUtils.class);
	
	/**
	 * <p>工具类禁止实例化</p>
	 */
	private UrlUtils() {
	}
	
	/**
	 * <p>对{@code content}进行URL编码</p>
	 * 
	 * @param content 待编码内容
	 * 
	 * @return 编码后内容
	 */
	public static final String encode(String content) {
		try {
			return URLEncoder
				.encode(content, SystemConfig.DEFAULT_CHARSET)
				.replace("+", "%20"); // 空格变成加号
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("URL编码异常：{}", content, e);
		}
		return content;
	}

	/**
	 * <p>对{@code content}进行URL解码</p>
	 * 
	 * @param content 待解码内容
	 * 
	 * @return 解码后内容
	 */
	public static final String decode(String content) {
		try {
			return URLDecoder.decode(content, SystemConfig.DEFAULT_CHARSET);
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("URL解码异常：{}", content, e);
		}
		return content;
	}
	
}
