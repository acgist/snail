package com.acgist.snail.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.module.client.launch.ApplicationNotifyClient;
import com.acgist.snail.module.server.ApplicationServer;
import com.acgist.snail.window.menu.TrayMenu;

import javafx.application.Platform;

/**
 * 平台工具
 */
public class PlatformUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(PlatformUtils.class);
	
	/**
	 * 退出平台
	 */
	public static final void exit() {
		LOGGER.info("系统关闭中");
		TrayMenu.exit();
		Platform.exit();
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
			LOGGER.info("项目已经启动，唤醒主窗口");
			ApplicationNotifyClient.notifyWindow();
		}
		return ok;
	}
	
}
