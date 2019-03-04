package com.acgist.snail.utils;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;

/**
 * utils - 浏览器
 */
public class BrowseUtils {

	private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(BrowseUtils.class);
	
	/**
	 * 打开网页链接
	 */
	public static final void open(String url) {
		try {
			Desktop.getDesktop().browse(new URI(url));
		} catch (IOException | URISyntaxException e) {
			LOGGER.error("打开网页链接异常");
		}
	}
	
}
