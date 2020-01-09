package com.acgist.snail.utils;

import java.awt.Desktop;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>浏览器工具</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class BrowseUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(BrowseUtils.class);
	
	/**
	 * <p>工具类禁止实例化</p>
	 */
	private BrowseUtils() {
	}
	
	/**
	 * <p>浏览器打开网页</p>
	 * 
	 * @param url URL地址
	 */
	public static final void open(final String url) {
		try {
			Desktop.getDesktop().browse(URI.create(url));
		} catch (Exception e) {
			LOGGER.error("浏览器打开网页异常：{}", url, e);
		}
	}
	
}
