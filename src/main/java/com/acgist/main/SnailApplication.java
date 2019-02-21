package com.acgist.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.utils.PlatformUtils;
import com.acgist.snail.window.main.MainWindow;

/**
 * 启动类
 */
public class SnailApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(SnailApplication.class);
	
	public static void main(String[] args) {
		LOGGER.info("蜗牛开始启动");
		if(listen()) {
			buildWindow(args);
		}
		LOGGER.info("蜗牛启动完成");
	}

	/**
	 * 检测是否已经存在进程
	 */
	private static final boolean listen() {
		return PlatformUtils.listen();
	}
	
	/**
	 * 创建窗口
	 */
	private static final void buildWindow(String[] args) {
		LOGGER.info("开始初始化窗口");
		Thread thread = new Thread(() -> {
			MainWindow.main(args);
		});
		thread.setName("蜗牛窗口");
		thread.start();
	}
	
}