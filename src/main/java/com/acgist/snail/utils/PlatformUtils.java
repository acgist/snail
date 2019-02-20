package com.acgist.snail.utils;

import java.awt.SystemTray;
import java.awt.TrayIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.aio.client.ApplicationNotifyClient;
import com.acgist.snail.aio.server.ApplicationServer;

import javafx.application.Platform;

/**
 * 平台工具
 */
public class PlatformUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(PlatformUtils.class);
	
	/**
	 * 退出平台
	 */
	public static final void exit(TrayIcon trayIcon) {
		SystemTray.getSystemTray().remove(trayIcon);
		Platform.exit();
	}
	
	/**
	 * 启动加载
	 */
	public static final void loading() {
	}
	
	/**
	 * 开启监听：开启失败表示已经启动了一个项目，唤醒之前的窗口
	 */
	public static final boolean listen() {
		boolean ok = ApplicationServer.getInstance().listen();
		if(!ok) {
			LOGGER.info("项目已经启动启动主窗口");
			ApplicationNotifyClient.notifyWindow();
		}
		return ok;
	}
	
}
