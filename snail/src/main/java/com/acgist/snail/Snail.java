package com.acgist.snail;

import java.util.ArrayList;
import java.util.List;

import com.acgist.snail.config.DhtConfig;
import com.acgist.snail.config.TrackerConfig;
import com.acgist.snail.context.EntityContext;
import com.acgist.snail.context.NatContext;
import com.acgist.snail.context.ProtocolContext;
import com.acgist.snail.context.TaskContext;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.context.initializer.ConfigInitializer;
import com.acgist.snail.context.initializer.DhtInitializer;
import com.acgist.snail.context.initializer.EntityInitializer;
import com.acgist.snail.context.initializer.Initializer;
import com.acgist.snail.context.initializer.LocalServiceDiscoveryInitializer;
import com.acgist.snail.context.initializer.NatInitializer;
import com.acgist.snail.context.initializer.TaskInitializer;
import com.acgist.snail.context.initializer.TorrentInitializer;
import com.acgist.snail.context.initializer.TrackerInitializer;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.application.ApplicationClient;
import com.acgist.snail.net.application.ApplicationServer;
import com.acgist.snail.net.torrent.TorrentServer;
import com.acgist.snail.net.torrent.lsd.LocalServiceDiscoveryServer;
import com.acgist.snail.net.torrent.peer.PeerServer;
import com.acgist.snail.net.torrent.tracker.TrackerServer;
import com.acgist.snail.net.torrent.utp.UtpRequestQueue;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.protocol.ftp.FtpProtocol;
import com.acgist.snail.protocol.hls.HlsProtocol;
import com.acgist.snail.protocol.http.HttpProtocol;
import com.acgist.snail.protocol.magnet.MagnetProtocol;
import com.acgist.snail.protocol.thunder.ThunderProtocol;
import com.acgist.snail.protocol.torrent.TorrentProtocol;

/**
 * <p>Snail下载工具</p>
 * 
 * @author acgist
 */
public final class Snail {

	private static final Logger LOGGER = LoggerFactory.getLogger(Snail.class);
	
	private static final Snail INSTANCE = new Snail();
	
	public static final Snail getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>是否添加下载完成等待锁</p>
	 */
	private boolean lock = false;
	/**
	 * <p>是否加载已有任务</p>
	 */
	private boolean buildTask = false;
	/**
	 * <p>是否加载Torrent协议</p>
	 */
	private boolean buildTorrent = false;
	/**
	 * <p>是否启动系统监听</p>
	 * <p>启动检测：开启监听失败表示已经存在系统实例（发送消息唤醒已有实例窗口）</p>
	 */
	private boolean buildApplication = false;
	/**
	 * <p>系统是否可用</p>
	 */
	private volatile boolean available = false;
	
	private Snail() {
		// 实体优先同步加载
		EntityInitializer.newInstance().sync();
		ConfigInitializer.newInstance().sync();
	}
	
	/**
	 * <p>新建下载任务</p>
	 * 
	 * @param url 下载链接
	 * 
	 * @return 下载任务
	 * 
	 * @throws DownloadException 下载异常
	 * 
	 * @see TaskContext#download(String)
	 */
	public ITaskSession download(String url) throws DownloadException {
		return TaskContext.getInstance().download(url);
	}
	
	/**
	 * <p>添加下载完成等待锁</p>
	 */
	public void lockDownload() {
		final TaskContext context = TaskContext.getInstance();
		if(context.downloading()) {
			synchronized (this) {
				this.lock = true;
				while(context.downloading()) {
					try {
						this.wait();
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						LOGGER.debug("线程等待异常", e);
					}
				}
			}
		}
	}

	/**
	 * <p>解除下载完成等待锁</p>
	 */
	public void unlockDownload() {
		if(this.lock) {
			synchronized (this) {
				this.notifyAll();
			}
		}
	}
	
	/**
	 * <p>判断系统是否可用</p>
	 * 
	 * @return 是否可用
	 */
	public static final boolean available() {
		return INSTANCE.available;
	}
	
	/**
	 * <p>关闭资源</p>
	 */
	public static final void shutdown() {
		if(INSTANCE.available) {
			INSTANCE.available = false;
			if(INSTANCE.buildApplication) {
				ApplicationServer.getInstance().close();
			}
			// 优先关闭任务
			TaskContext.getInstance().shutdown();
			// 释放Torrent协议
			if(INSTANCE.buildTorrent) {
				PeerServer.getInstance().close();
				TorrentServer.getInstance().close();
				TrackerServer.getInstance().close();
				LocalServiceDiscoveryServer.getInstance().close();
				NatContext.getInstance().shutdown();
				UtpRequestQueue.getInstance().shutdown();
				DhtConfig.getInstance().persistent();
				TrackerConfig.getInstance().persistent();
			}
			EntityContext.getInstance().persistent();
		}
	}
	
	/**
	 * <p>SnailBuilder</p>
	 * 
	 * @author acgist
	 */
	public static final class SnailBuilder {
		
		/**
		 * <p>创建SnailBuilder</p>
		 * 
		 * @return {@link SnailBuilder}
		 */
		public static final SnailBuilder newBuilder() {
			return new SnailBuilder();
		}
		
		private SnailBuilder() {
		}

		/**
		 * <p>同步创建Snail</p>
		 * 
		 * @return {@link Snail}
		 */
		public Snail buildSync() {
			return this.build(true);
		}
		
		/**
		 * <p>异步创建Snail</p>
		 * 
		 * @return {@link Snail}
		 */
		public Snail buildAsyn() {
			return this.build(false);
		}
		
		/**
		 * <p>创建Snail</p>
		 * 
		 * @param sync 是否同步创建
		 * 
		 * @return {@link Snail}
		 * 
		 * @throws DownloadException 下载异常
		 */
		public synchronized Snail build(boolean sync) {
			LOGGER.debug("创建Snail：{}", sync);
			if(INSTANCE.available) {
				return INSTANCE;
			}
			INSTANCE.available = true;
			if(INSTANCE.buildApplication) {
				INSTANCE.available = ApplicationServer.getInstance().listen();
			}
			if(INSTANCE.available) {
				ProtocolContext.getInstance().available(INSTANCE.available);
				this.buildInitializers().forEach(initializer -> {
					if(sync) {
						initializer.sync();
					} else {
						initializer.asyn();
					}
				});
			} else {
				LOGGER.debug("已有系统实例：唤醒实例窗口");
				ApplicationClient.notifyWindow();
			}
			return INSTANCE;
		}

		/**
		 * <p>加载初始化列表</p>
		 * 
		 * @return 初始化列表
		 */
		private List<Initializer> buildInitializers() {
			final List<Initializer> list = new ArrayList<>();
			if(INSTANCE.buildTorrent) {
				list.add(NatInitializer.newInstance());
				list.add(DhtInitializer.newInstance());
				list.add(TorrentInitializer.newInstance());
				list.add(TrackerInitializer.newInstance());
				list.add(LocalServiceDiscoveryInitializer.newInstance());
			}
			if(INSTANCE.buildTask) {
				list.add(TaskInitializer.newInstance());
			}
			return list;
		}
		
		/**
		 * <p>加载已有任务</p>
		 * 
		 * @return {@link SnailBuilder}
		 */
		public SnailBuilder loadTask() {
			INSTANCE.buildTask = true;
			return this;
		}

		/**
		 * <p>启动系统监听</p>
		 * 
		 * @return {@link SnailBuilder}
		 */
		public SnailBuilder application() {
			INSTANCE.buildApplication = true;
			return this;
		}
		
		/**
		 * <p>注册下载协议</p>
		 * 
		 * @param protocol 下载协议
		 * 
		 * @return {@link SnailBuilder}
		 */
		public SnailBuilder register(Protocol protocol) {
			ProtocolContext.getInstance().register(protocol);
			return this;
		}
		
		/**
		 * <p>注册FTP下载协议</p>
		 * 
		 * @return {@link SnailBuilder}
		 */
		public SnailBuilder enableFtp() {
			return this.register(FtpProtocol.getInstance());
		}
		
		/**
		 * <p>注册HLS下载协议</p>
		 * 
		 * @return {@link SnailBuilder}
		 */
		public SnailBuilder enableHls() {
			return this.register(HlsProtocol.getInstance());
		}
		
		/**
		 * <p>注册HTTP下载协议</p>
		 * 
		 * @return {@link SnailBuilder}
		 */
		public SnailBuilder enableHttp() {
			return this.register(HttpProtocol.getInstance());
		}
		
		/**
		 * <p>注册Magnet下载协议</p>
		 * 
		 * @return {@link SnailBuilder}
		 */
		public SnailBuilder enableMagnet() {
			INSTANCE.buildTorrent = true;
			return this.register(MagnetProtocol.getInstance());
		}
		
		/**
		 * <p>注册Thunder下载协议</p>
		 * 
		 * @return {@link SnailBuilder}
		 */
		public SnailBuilder enableThunder() {
			return this.register(ThunderProtocol.getInstance());
		}
		
		/**
		 * <p>注册Torrent下载协议</p>
		 * 
		 * @return {@link SnailBuilder}
		 */
		public SnailBuilder enableTorrent() {
			INSTANCE.buildTorrent = true;
			return this.register(TorrentProtocol.getInstance());
		}
		
		/**
		 * <p>注册所有协议</p>
		 * 
		 * @return {@link SnailBuilder}
		 */
		public SnailBuilder enableAllProtocol() {
			return this
				.enableFtp()
				.enableHls()
				.enableHttp()
				.enableMagnet()
				.enableThunder()
				.enableTorrent();
		}
		
	}
	
}
