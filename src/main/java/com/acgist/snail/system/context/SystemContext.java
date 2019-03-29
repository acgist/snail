package com.acgist.snail.system.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.initializer.impl.ConfigInitializer;
import com.acgist.snail.system.initializer.impl.DbInitializer;
import com.acgist.snail.system.initializer.impl.DownloaderInitializer;
import com.acgist.snail.system.initializer.impl.ProtocolInitializer;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.NetUtils;

/**
 * 系统上下文
 */
public class SystemContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemContext.class);

	/**
	 * 系统状态
	 */
	private static boolean shutdown = false;
	
	/**
	 * 系统初始化
	 */
	public static final void init() {
		LOGGER.info("系统初始化");
		DbInitializer.newInstance().initSync();
		ProtocolInitializer.newInstance().initAsyn();
		DownloaderInitializer.newInstance().initAsyn();
		ConfigInitializer.newInstance().initAsyn();
	}
	
	/**
	 * 系统信息
	 */
	public static final void info() {
		final var runtime = Runtime.getRuntime();
		LOGGER.info("操作系统名称：{}", System.getProperty("os.name"));
		LOGGER.info("操作系统架构：{}", System.getProperty("os.arch"));
		LOGGER.info("操作系统版本：{}", System.getProperty("os.version"));
		LOGGER.info("操作系统可用处理器数量：{}", runtime.availableProcessors());
		LOGGER.info("Java版本：{}", System.getProperty("java.version"));
		LOGGER.info("Java主目录：{}", System.getProperty("java.home"));
		LOGGER.info("虚拟机名称：{}", System.getProperty("java.vm.name"));
		LOGGER.info("虚拟机最大内存：{}", FileUtils.formatSize(runtime.maxMemory()));
		LOGGER.info("虚拟机已用内存：{}", FileUtils.formatSize(runtime.totalMemory()));
		LOGGER.info("虚拟机空闲内存：{}", FileUtils.formatSize(runtime.freeMemory()));
		LOGGER.info("用户主目录：{}", System.getProperty("user.home"));
		LOGGER.info("用户工作目录：{}", System.getProperty("user.dir"));
		LOGGER.info("文件编码：{}", System.getProperty("file.encoding"));
		LOGGER.info("本机名称：{}", NetUtils.inetHostName());
		LOGGER.info("本机地址：{}", NetUtils.inetHostAddress());
	}
	
	/**
	 * 系统是否可用
	 */
	public static final boolean available() {
		return !shutdown;
	}

	/**
	 * 关闭系统
	 */
	public static final void shutdown() {
		shutdown = true;
	}
	
}
