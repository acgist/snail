package com.acgist.snail.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.protocol.Protocol;

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
	
	/**
	 * <p>获取跳转链接完整路径</p>
	 * 
	 * @param source 原始页面链接
	 * @param target 目标页面链接
	 * 
	 * @return 完整链接
	 */
	public static final String redirect(final String source, String target) {
		Objects.requireNonNull(source, "原始页面链接不能为空");
		Objects.requireNonNull(target, "目标页面链接不能为空");
		// 去掉引号
		if(target.startsWith("\"")) {
			target = target.substring(1);
		}
		if(target.endsWith("\"")) {
			target = target.substring(0, target.length() - 1);
		}
		// 执行跳转
		if(Protocol.Type.HTTP.verify(target)) {
			// 完整连接
			return target;
		} else if(target.startsWith("/")) {
			// 绝对目录链接
			final String prefix = Protocol.Type.HTTP.prefix(source);
			final int index = source.indexOf('/', prefix.length());
			if(index > prefix.length()) {
				return source.substring(0, index) + target;
			} else {
				return source + target;
			}
		} else {
			// 相对目录链接
			final String prefix = Protocol.Type.HTTP.prefix(source);
			final int index = source.lastIndexOf('/');
			if(index > prefix.length()) {
				return source.substring(0, index) + "/" + target;
			} else {
				return source + "/" + target;
			}
		}
	}
	
}
