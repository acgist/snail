package com.acgist.snail.context.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.DownloaderManager;

/**
 * <p>初始化下载器</p>
 * 
 * @author acgist
 */
public final class DownloaderInitializer extends Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DownloaderInitializer.class);
	
	/**
	 * <p>禁止创建实例</p>
	 */
	private DownloaderInitializer() {
	}
	
	public static final DownloaderInitializer newInstance() {
		return new DownloaderInitializer();
	}
	
	@Override
	protected void init() {
		LOGGER.info("初始化下载器");
		DownloaderManager.getInstance().loadTaskEntity();
	}

}
