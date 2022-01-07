package com.acgist.snail.gui.javafx;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.swing.SwingUtilities;

import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>桌面助手</p>
 * 
 * @author acgist
 */
public final class Desktops {

	private static final Logger LOGGER = LoggerFactory.getLogger(Desktops.class);
	
	private Desktops() {
	}
	
	/**
	 * <p>打开文件</p>
	 * <p>使用资源管理器打开文件</p>
	 * 
	 * @param file 文件
	 */
	public static final void open(final File file) {
		if(
			file != null &&
			file.exists() &&
			support(Action.OPEN)
		) {
			SwingUtilities.invokeLater(() -> {
				try {
					Desktop.getDesktop().open(file);
				} catch (IOException e) {
					LOGGER.error("打开文件异常", e);
				}
			});
		} else {
			LOGGER.warn("打开文件失败：{}", file);
		}
	}
	
	/**
	 * <p>打开网页链接</p>
	 * <p>使用默认浏览器打开网页链接</p>
	 * 
	 * @param url 网页链接
	 */
	public static final void browse(final String url) {
		if(
			StringUtils.isNotEmpty(url) &&
			support(Action.BROWSE)
		) {
			SwingUtilities.invokeLater(() -> {
				try {
					Desktop.getDesktop().browse(URI.create(url));
				} catch (IOException e) {
					LOGGER.error("打开网页链接异常：{}", url, e);
				}
			});
		} else {
			LOGGER.warn("打开网页链接失败：{}", url);
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
