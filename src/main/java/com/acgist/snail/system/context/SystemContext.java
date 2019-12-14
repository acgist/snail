package com.acgist.snail.system.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.DownloaderManager;
import com.acgist.snail.gui.GuiHandler;
import com.acgist.snail.net.TcpClient;
import com.acgist.snail.net.TcpServer;
import com.acgist.snail.net.UdpServer;
import com.acgist.snail.net.application.ApplicationClient;
import com.acgist.snail.net.application.ApplicationServer;
import com.acgist.snail.net.torrent.TorrentServer;
import com.acgist.snail.net.torrent.lsd.LocalServiceDiscoveryServer;
import com.acgist.snail.net.torrent.peer.PeerServer;
import com.acgist.snail.net.torrent.tracker.TrackerServer;
import com.acgist.snail.net.torrent.utp.bootstrap.UtpRequestQueue;
import com.acgist.snail.net.web.WebServer;
import com.acgist.snail.repository.DatabaseManager;
import com.acgist.snail.system.config.DhtConfig;
import com.acgist.snail.system.config.TrackerConfig;
import com.acgist.snail.system.initializer.impl.ConfigInitializer;
import com.acgist.snail.system.initializer.impl.DatabaseInitializer;
import com.acgist.snail.system.initializer.impl.DhtInitializer;
import com.acgist.snail.system.initializer.impl.DownloaderInitializer;
import com.acgist.snail.system.initializer.impl.LocalServiceDiscoveryInitializer;
import com.acgist.snail.system.initializer.impl.NatInitializer;
import com.acgist.snail.system.initializer.impl.PeerInitializer;
import com.acgist.snail.system.initializer.impl.ProtocolInitializer;
import com.acgist.snail.system.initializer.impl.TorrentInitializer;
import com.acgist.snail.system.initializer.impl.TrackerInitializer;
import com.acgist.snail.system.initializer.impl.WebServerInitializer;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.LoggerUtils;
import com.acgist.snail.utils.NetUtils;

/**
 * <p>系统上下文</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class SystemContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemContext.class);

	/**
	 * <p>系统状态</p>
	 */
	private static boolean shutdown = false;
	/**
	 * <p>系统名称</p>
	 */
	private static String osName;
	
	/**
	 * <p>开启系统监听</p>
	 * <p>启动检测：开启监听失败表示已经启动了一个系统实例，发送消息唤醒之前的实例窗口。</p>
	 */
	public static final boolean listen() {
		final boolean ok = ApplicationServer.getInstance().listen();
		if(!ok) {
			LOGGER.info("已有系统实例，唤醒实例窗口。");
			ApplicationClient.notifyWindow();
		}
		return ok;
	}
	
	/**
	 * <p>系统初始化</p>
	 * <p>数据库必须优先同步初始化</p>
	 */
	public static final void init() {
		LOGGER.info("系统初始化");
		// 同步
		DatabaseInitializer.newInstance().sync();
		// 异步
		ConfigInitializer.newInstance().asyn();
		ProtocolInitializer.newInstance().asyn();
		NatInitializer.newInstance().asyn();
		DhtInitializer.newInstance().asyn();
		PeerInitializer.newInstance().asyn();
		TrackerInitializer.newInstance().asyn();
		TorrentInitializer.newInstance().asyn();
		DownloaderInitializer.newInstance().asyn();
		WebServerInitializer.newInstance().asyn();
		LocalServiceDiscoveryInitializer.newInstance().asyn();
	}
	
	/**
	 * <p>系统信息</p>
	 */
	public static final void info() {
		final var runtime = Runtime.getRuntime();
		LOGGER.info("操作系统名称：{}", System.getProperty("os.name"));
		LOGGER.info("操作系统架构：{}", System.getProperty("os.arch"));
		LOGGER.info("操作系统版本：{}", System.getProperty("os.version"));
		LOGGER.info("操作系统可用处理器数量：{}", runtime.availableProcessors());
		LOGGER.info("Java版本：{}", System.getProperty("java.version"));
		LOGGER.info("Java主目录：{}", System.getProperty("java.home"));
		LOGGER.info("Java包目录：{}", System.getProperty("java.library.path"));
		LOGGER.info("虚拟机名称：{}", System.getProperty("java.vm.name"));
		LOGGER.info("虚拟机最大内存：{}", FileUtils.formatSize(runtime.maxMemory()));
		LOGGER.info("虚拟机已用内存：{}", FileUtils.formatSize(runtime.totalMemory()));
		LOGGER.info("虚拟机空闲内存：{}", FileUtils.formatSize(runtime.freeMemory()));
		LOGGER.info("用户主目录：{}", System.getProperty("user.home"));
		LOGGER.info("用户工作目录：{}", System.getProperty("user.dir"));
		LOGGER.info("文件编码：{}", System.getProperty("file.encoding"));
		LOGGER.info("本机名称：{}", NetUtils.localHostName());
		LOGGER.info("本机地址：{}", NetUtils.localHostAddress());
	}

	/**
	 * <p>系统关闭</p>
	 * <p>创建的所有线程均是守护线程，所以可以不用手动关闭。</p>
	 */
	public static final void shutdown() {
		if(SystemContext.available()) {
			SystemContext.shutdown = true;
			SystemThreadContext.submit(() -> {
				LOGGER.info("系统关闭中...");
				GuiHandler.getInstance().hide();
				DownloaderManager.getInstance().shutdown();
				NatContext.getInstance().shutdown();
				PeerServer.getInstance().close();
				TrackerServer.getInstance().close();
				TorrentServer.getInstance().close();
				ApplicationServer.getInstance().close();
				LocalServiceDiscoveryServer.getInstance().close();
				TcpClient.shutdown();
				TcpServer.shutdown();
				UdpServer.shutdown();
				WebServer.getInstance().shutdown();
				UtpRequestQueue.getInstance().shutdown();
				DatabaseManager.getInstance().shutdown();
				DhtConfig.getInstance().persistent();
				TrackerConfig.getInstance().persistent();
				GuiHandler.getInstance().exit();
				SystemThreadContext.shutdown();
				LOGGER.info("系统已关闭");
				LoggerUtils.shutdown();
			});
		} else {
			GuiHandler.getInstance().alert("关闭提示", "系统正在关闭中...");
		}
	}

	/**
	 * <p>系统是否可用</p>
	 */
	public static final boolean available() {
		return !SystemContext.shutdown;
	}

	/**
	 * <p>系统名称</p>
	 */
	public static final String osName() {
		if(SystemContext.osName == null) {
			SystemContext.osName = System.getProperty("os.name");
		}
		return SystemContext.osName;
	}
	
}
