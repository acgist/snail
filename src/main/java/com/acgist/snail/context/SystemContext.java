package com.acgist.snail.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.DownloaderManager;
import com.acgist.snail.module.initializer.TableInitializer;

/**
 * 系统上下文
 */
public class SystemContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemContext.class);

	/**
	 * 系统初始化
	 */
	public static final void init() {
		LOGGER.info("系统初始化");
		TableInitializer.init();
		DownloaderManager.getInstance();
	}
	
}
