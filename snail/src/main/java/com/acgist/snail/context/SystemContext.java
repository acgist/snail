package com.acgist.snail.context;

import java.util.concurrent.TimeUnit;

import com.acgist.snail.Snail;
import com.acgist.snail.Snail.SnailBuilder;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.format.JSON;
import com.acgist.snail.gui.GuiContext;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.NetException;
import com.acgist.snail.net.TcpClient;
import com.acgist.snail.net.TcpServer;
import com.acgist.snail.net.UdpServer;
import com.acgist.snail.net.http.HttpClient;
import com.acgist.snail.utils.FileUtils;

/**
 * 系统上下文
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
	 * 系统类型
	 * 
	 * @author acgist
	 */
	public enum SystemType {
		
		/**
		 * Mac
		 */
		MAC("Mac OS", "Mac OS X"),
		/**
		 * Linux
		 */
		LINUX("Linux"),
		/**
		 * Windows
		 */
		WINDOWS("Windows XP", "Windows Vista", "Windows 7", "Windows 10"),
		/**
		 * Android
		 */
		ANDROID("Android");
		
		/**
		 * 系统名称
		 */
		private final String[] osNames;

		/**
		 * @param osNames 系统名称
		 */
		private SystemType(String ... osNames) {
			this.osNames = osNames;
		}

		/**
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
	 * 系统名称
	 */
	private final String osName;
	
	private SystemContext() {
		this.osName = System.getProperty("os.name");
	}
	
	/**
	 * 整理系统内存
	 */
	public static final void gc() {
		LOGGER.info("整理系统内存");
		System.gc();
	}
	
	/**
	 * 系统信息
	 */
	public static final void info() {
		LOGGER.info(
			"""
			
			
			:: Snail : 碧螺萧萧
			:: https://gitee.com/acgist/snail
			
			月落乌啼霜满天，江枫渔火对愁眠。
			姑苏城外寒山寺，夜半钟声到客船。
			"""
		);
		final var runtime = Runtime.getRuntime();
		LOGGER.info("操作系统名称：{}", System.getProperty("os.name"));
		LOGGER.info("操作系统架构：{}", System.getProperty("os.arch"));
		LOGGER.info("操作系统版本：{}", System.getProperty("os.version"));
		LOGGER.info("操作系统可用处理器数量：{}", runtime.availableProcessors());
		LOGGER.info("Java版本：{}", System.getProperty("java.version"));
		LOGGER.info("Java主目录：{}", System.getProperty("java.home"));
		LOGGER.info("Java库目录：{}", System.getProperty("java.library.path"));
		LOGGER.info("Java临时文件目录：{}", System.getProperty("java.io.tmpdir"));
		LOGGER.info("Java Class目录：{}", System.getProperty("java.class.path"));
		LOGGER.info("虚拟机名称：{}", System.getProperty("java.vm.name"));
		LOGGER.info("虚拟机版本：{}", System.getProperty("java.vm.version"));
		LOGGER.info("虚拟机空闲内存：{}", FileUtils.formatSize(runtime.freeMemory()));
		LOGGER.info("虚拟机已用内存：{}", FileUtils.formatSize(runtime.totalMemory()));
		LOGGER.info("虚拟机最大内存：{}", FileUtils.formatSize(runtime.maxMemory()));
		LOGGER.info("虚拟机运行时名称：{}", System.getProperty("java.runtime.name"));
		LOGGER.info("虚拟机运行时版本：{}", System.getProperty("java.runtime.version"));
		LOGGER.info("用户目录：{}", System.getProperty("user.home"));
		LOGGER.info("用户名称：{}", System.getProperty("user.name"));
		LOGGER.info("用户国家：{}", System.getProperty("user.country"));
		LOGGER.info("用户语言：{}", System.getProperty("user.language"));
		LOGGER.info("用户时区：{}", System.getProperty("user.timezone"));
		LOGGER.info("用户工作目录：{}", System.getProperty("user.dir"));
		LOGGER.info("文件编码：{}", System.getProperty("file.encoding"));
		LOGGER.info("本地编码：{}", System.getProperty("native.encoding"));
		LOGGER.info("CPU大小端：{}", System.getProperty("sun.cpu.endian"));
		LOGGER.info("CPU指令集：{}", System.getProperty("sun.cpu.isalist"));
	}
	
	/**
	 * 系统初始化
	 * 
	 * @return {@link Snail}
	 */
	public static final Snail build() {
		return SnailBuilder.newBuilder()
			.loadTask()
			.application()
			.enableAllProtocol()
			.buildAsyn();
	}
	
	/**
	 * 系统关闭
	 * 所有线程都是守护线程，所以可以不用手动关闭。
	 */
	public static final void shutdown() {
		if(Snail.available()) {
			SystemThreadContext.submit(() -> {
				LOGGER.info("系统关闭中");
				// 关闭GUI
				GuiContext.getInstance().hide();
				GuiContext.getInstance().exit();
				// 关闭Snail
				Snail.shutdown();
				// 关闭TCP/UDP
				TcpClient.shutdown();
				TcpServer.shutdown();
				UdpServer.shutdown();
				// 关闭线程池
				SystemThreadContext.shutdown();
				LOGGER.info("系统已关闭");
				LoggerFactory.shutdown();
			});
			// 设置强制关闭程序定时任务
			SystemThreadContext.scheduled(SystemConfig.SHUTDOWN_FORCE_TIME, TimeUnit.SECONDS, () -> {
				LOGGER.warn("系统强制关闭");
				LoggerFactory.shutdown();
				Runtime.getRuntime().halt(0);
			});
		} else {
			GuiContext.getInstance().alert("关闭提示", "系统关闭中");
		}
	}
	
	/**
	 * @return 系统名称
	 */
	public static final String osName() {
		return INSTANCE.osName;
	}

	/**
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
			LOGGER.error("版本检查异常", e);
			GuiContext.getInstance().alert("版本检查失败", e.getMessage());
		}
		return true;
	}
	
}
