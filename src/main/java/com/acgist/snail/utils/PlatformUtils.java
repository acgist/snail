package com.acgist.snail.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.DownloaderManager;
import com.acgist.snail.gui.menu.TrayMenu;
import com.acgist.snail.net.AbstractTcpClient;
import com.acgist.snail.net.AbstractTcpServer;
import com.acgist.snail.net.AbstractUdpClient;
import com.acgist.snail.net.application.ApplicationClient;
import com.acgist.snail.net.application.ApplicationServer;
import com.acgist.snail.system.context.SystemThreadContext;

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
		/**系统线程都是后台线程以下操作可以不执行**/
		AbstractUdpClient.shutdown();
		AbstractTcpClient.shutdown();
		ApplicationServer.getInstance().close();
		AbstractTcpServer.shutdown();
		DownloaderManager.getInstance().shutdown();
		SystemThreadContext.shutdown();
		/**系统线程都是后台线程以上操作可以不执行**/
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
