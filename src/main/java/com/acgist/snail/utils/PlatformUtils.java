package com.acgist.snail.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.DownloaderManager;
import com.acgist.snail.net.client.application.ApplicationClient;
import com.acgist.snail.net.server.impl.ApplicationServer;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.window.main.TaskTimer;
import com.acgist.snail.window.menu.TrayMenu;

import javafx.application.Platform;

/**
 * utils - 平台
 */
public class PlatformUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(PlatformUtils.class);
	
	/**
	 * 退出平台
	 */
	public static final void exit() {
		LOGGER.info("系统关闭中");
		TaskTimer.getInstance().shutdown();
		ApplicationServer.getInstance().shutdown();
		DownloaderManager.getInstance().shutdown();
		SystemThreadContext.shutdown();
		Platform.exit();
		TrayMenu.exit();
		LOGGER.info("系统已关闭");
	}
	
	/**
	 * 启动加载
	 */
	public static final void loading() {
		// TODO：加载效果
	}
	
	/**
	 * 开启监听：开启失败表示已经启动了一个项目，唤醒之前的窗口
	 */
	public static final boolean listen() {
		boolean ok = ApplicationServer.getInstance().listen();
		if(!ok) {
			LOGGER.info("已有系统实例，唤醒系统窗口");
			ApplicationClient.notifyWindow();
		}
		return ok;
	}
	
}
