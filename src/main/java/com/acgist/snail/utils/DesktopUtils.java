package com.acgist.snail.utils;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>桌面工具</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class DesktopUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(DesktopUtils.class);
	
	/**
	 * <p>工具类禁止实例化</p>
	 */
	private DesktopUtils() {
	}
	
	/**
	 * <p>打开文件</p>
	 * <p>使用资源管理器打开文件</p>
	 * 
	 * @param file 文件
	 */
	public static final void open(final File file) {
		if(support(Action.OPEN)) {
			try {
				Desktop.getDesktop().open(file);
			} catch (IOException e) {
				LOGGER.error("资源管理器打开文件异常", e);
			}
		} else if(support(Action.BROWSE_FILE_DIR)) {
			Desktop.getDesktop().browseFileDirectory(file);
		} else {
			LOGGER.info("系统不支持：{}", file.getPath());
		}
	}
	
	/**
	 * <p>打开网页链接</p>
	 * <p>使用默认浏览器打开网页链接</p>
	 * 
	 * @param url 网页链接
	 */
	public static final void browse(final String url) {
		if(support(Action.BROWSE)) {
			try {
				Desktop.getDesktop().browse(URI.create(url));
			} catch (IOException e) {
				LOGGER.error("浏览器打开网页异常：{}", url, e);
			}
		} else {
			LOGGER.info("系统不支持：{}", url);
		}
	}
	
	/**
	 * <p>判断是否支持动作</p>
	 * 
	 * @param action 动作
	 * 
	 * @return 是否支持
	 */
	public static final boolean support(final Action action) {
		return
			Desktop.isDesktopSupported() &&
			Desktop.getDesktop().isSupported(action);
	}
	
}
