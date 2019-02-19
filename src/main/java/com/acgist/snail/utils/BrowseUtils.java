package com.acgist.snail.utils;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;

/**
 * 浏览器工具
 */
public class BrowseUtils {

	private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(BrowseUtils.class);
	
	
	public static final void open(String url) {
		try {
			Desktop.getDesktop().browse(new URI(url));
		} catch (IOException | URISyntaxException e) {
			LOGGER.error("打开网页链接异常");
		}
	}
	
}
