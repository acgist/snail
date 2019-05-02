package com.acgist.snail.utils;

import java.awt.Desktop;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 浏览器工具
 */
public class BrowseUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(BrowseUtils.class);
	
	/**
	 * 浏览器打开网页
	 */
	public static final void open(final String url) {
		try {
			Desktop.getDesktop().browse(URI.create(url));
		} catch (Exception e) {
			LOGGER.error("浏览器打开网页异常：{}", url, e);
		}
	}
	
}
