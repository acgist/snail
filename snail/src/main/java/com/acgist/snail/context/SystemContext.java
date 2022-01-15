package com.acgist.snail.context;

import java.util.concurrent.TimeUnit;

import com.acgist.snail.IContext;
import com.acgist.snail.Snail;
import com.acgist.snail.Snail.SnailBuilder;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.format.JSON;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.TcpClient;
import com.acgist.snail.net.TcpServer;
import com.acgist.snail.net.UdpServer;
import com.acgist.snail.net.http.HttpClient;
import com.acgist.snail.utils.FileUtils;

/**
 * <p>系统上下文</p>
 * 
 * @author acgist
 */
public final class SystemContext implements IContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemContext.class);

	private static final SystemContext INSTANCE = new SystemContext();
	
	public static final SystemContext getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>系统类型</p>
	 * 
	 * @author acgist
	 */
	public enum SystemType {
		
		/**
		 * <p>Mac</p>
		 */
		MAC("Mac OS", "Mac OS X"),
		/**
		 * <p>Linux</p>
		 */
		LINUX("Linux"),
		/**
		 * <p>Windows</p>
		 */
		WINDOWS("Windows XP", "Windows Vista", "Windows 7", "Windows 10"),
		/**
		 * <p>Android</p>
		 */
		ANDROID("Android");
		
		/**
		 * <p>系统名称</p>
		 */
		private final String[] osNames;

		/**
		 * <p>系统类型</p>
		 * 
		 * @param osNames 系统名称
		 */
		private SystemType(String ... osNames) {
			this.osNames = osNames;
		}

		/**
		 * <p>获取系统类型</p>
		 * 
		 * @return 系统类型
		 */
		public static final SystemType local() {
			final String osName = SystemContext.osName();
			for (SystemType type : SystemType.values()) {
				for (String value : type.osNames) {
					if(value.equals(osName)) {
						return type;
					}
				}
			}
			LOGGER.warn("未知系统类型：{}", osName);
			return null;
		}
		
	}
	
	/**
	 * <p>系统名称</p>
	 */
	private final String osName;
	
	private SystemContext() {
		this.osName = System.getProperty("os.name");
	}
	
	/**
	 * <p>整理系统内存</p>
	 */
	public static final void gc() {
		LOGGER.info("整理系统内存");
		System.gc();
	}
	
	/**
	 * <p>系统信息</p>
	 */
	public static final void info() {
		final var runtime = Runtime.getRuntime();
		final String freeMemory = FileUtils.formatSize(runtime.freeMemory());
		final String totalMemory = FileUtils.formatSize(runtime.totalMemory());
		final String maxMemory = FileUtils.formatSize(runtime.maxMemory());
		LOGGER.info("操作系统名称：{}", System.getProperty("os.name"));
		LOGGER.info("操作系统架构：{}", System.getProperty("os.arch"));
		LOGGER.info("操作系统版本：{}", System.getProperty("os.version"));
		LOGGER.info("操作系统可用处理器数量：{}", runtime.availableProcessors());
		LOGGER.info("Java版本：{}", System.getProperty("java.version"));
		LOGGER.info("Java主目录：{}", System.getProperty("java.home"));
		LOGGER.info("Java库目录：{}", System.getProperty("java.library.path"));
		LOGGER.info("虚拟机名称：{}", System.getProperty("java.vm.name"));
		LOGGER.info("虚拟机空闲内存：{}", freeMemory);
		LOGGER.info("虚拟机已用内存：{}", totalMemory);
		LOGGER.info("虚拟机最大内存：{}", maxMemory);
		LOGGER.info("用户目录：{}", System.getProperty("user.home"));
		LOGGER.info("工作目录：{}", System.getProperty("user.dir"));
		LOGGER.info("文件编码：{}", System.getProperty("file.encoding"));
	}
	
	/**
	 * <p>系统初始化</p>
	 * 
	 * @return Snail
	 */
	public static final Snail build() {
		return SnailBuilder.newBuilder()
			.loadTask()
			.application()
			.enableAllProtocol()
			.buildAsyn();
	}
	
	/**
	 * <p>系统关闭</p>
	 * <p>所有线程都是守护线程，所以可以不用手动关闭。</p>
	 * 
	 * @see SystemThreadContext
	 */
	public static final void shutdown() {
		if(Snail.available()) {
			SystemThreadContext.submit(() -> {
				LOGGER.info("系统关闭中");
				GuiContext.getInstance().hide();
				Snail.shutdown();
				TcpClient.shutdown();
				TcpServer.shutdown();
				UdpServer.shutdown();
				GuiContext.getInstance().exit();
				SystemThreadContext.shutdown();
				LOGGER.info("系统已关闭");
				LoggerFactory.shutdown();
			});
			// 设置强制关闭程序定时任务
			SystemThreadContext.timer(SystemConfig.SHUTDOWN_FORCE_TIME, TimeUnit.SECONDS, () -> {
				LOGGER.warn("强制关闭程序");
				LoggerFactory.shutdown();
				Runtime.getRuntime().halt(0);
			});
		} else {
			GuiContext.getInstance().alert("关闭提示", "系统关闭中");
		}
	}
	
	/**
	 * <p>获取系统名称</p>
	 * 
	 * @return 系统名称
	 */
	public static final String osName() {
		return INSTANCE.osName;
	}

	/**
	 * <p>判断是不是最新版本</p>
	 * 
	 * @return 是不是最新版本
	 */
	public static final boolean latestRelease() {
		try {
			// 本地版本：1.0.0
			final String version = SystemConfig.getVersion();
			final var body = HttpClient
				.newInstance(SystemConfig.getLatestRelease())
				.get()
				.responseToString();
			final JSON json = JSON.ofString(body);
			// 最新版本：1.0.0
			final String latestVersion = json.getString("tag_name");
			LOGGER.debug("版本信息：{}-{}", version, latestVersion);
			return latestVersion.equals(version);
		} catch (NetException e) {
			LOGGER.error("获取版本信息异常", e);
		}
		return true;
	}
	
}
