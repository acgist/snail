package com.acgist.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.main.MainWindow;
import com.acgist.snail.gui.menu.TrayMenu;
import com.acgist.snail.system.context.SystemContext;
import com.acgist.snail.system.context.SystemThreadContext;

import javafx.application.Platform;

/**
 * <h1>Snail系统启动类。</h1>
 * <p>Snail（蜗牛）是一款下载软件，支持下载协议：BT、FTP、HTTP、ED2K。</p>
 * TODO：启动检测、端口、外网端口。
 * TODO：端口被关闭提示重启或者自动重新监听
 * TODO：问题：BT任务下载统计错误，内存泄露
 * 
 * @author acgist
 * @since 1.0.0
 */
public class Application {

	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
	
	/**
	 * 启动
	 */
	public static final void main(String[] args) {
		LOGGER.info("系统开始启动");
		SystemContext.info();
		final boolean enable = listen();
		if(enable) {
			initContext();
			buildWindow();
		}
		LOGGER.info("系统启动完成");
	}
	
	/**
	 * 启动系统监听
	 */
	private static final boolean listen() {
		return SystemContext.listen();
	}
	
	/**
	 * 初始化系统上下文
	 */
	private static final void initContext() {
		SystemContext.init();
	}
	
	/**
	 * 初始化JavaFX平台
	 */
	private static final void buildWindow() {
		LOGGER.info("初始化窗口");
		final Thread thread = new Thread();
		thread.setName(SystemThreadContext.SNAIL_THREAD_PLATFORM);
		thread.setDaemon(true);
		Platform.startup(thread);
		Platform.runLater(() -> {
			TrayMenu.getInstance();
			MainWindow.getInstance().show();
		});
	}

}