package com.acgist.snail.utils;

import java.awt.SystemTray;
import java.awt.TrayIcon;

import javafx.application.Platform;

/**
 * 品台工具
 */
public class PlatformUtils {

	/**
	 * 退出品台
	 */
	public static final void exit(TrayIcon trayIcon) {
		SystemTray.getSystemTray().remove(trayIcon);
		Platform.exit();
	}
	
	
	
}
