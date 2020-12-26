package com.acgist.snail;

import java.util.ArrayList;
import java.util.List;

import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.context.initializer.Initializer;
import com.acgist.snail.context.initializer.impl.ConfigInitializer;
import com.acgist.snail.context.initializer.impl.DhtInitializer;
import com.acgist.snail.context.initializer.impl.DownloaderInitializer;
import com.acgist.snail.context.initializer.impl.EntityInitializer;
import com.acgist.snail.context.initializer.impl.LocalServiceDiscoveryInitializer;
import com.acgist.snail.context.initializer.impl.NatInitializer;
import com.acgist.snail.context.initializer.impl.TorrentInitializer;
import com.acgist.snail.context.initializer.impl.TrackerInitializer;
import com.acgist.snail.downloader.DownloaderManager;
import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.protocol.ProtocolManager;
import com.acgist.snail.protocol.ftp.FtpProtocol;
import com.acgist.snail.protocol.hls.HlsProtocol;
import com.acgist.snail.protocol.http.HttpProtocol;
import com.acgist.snail.protocol.magnet.MagnetProtocol;
import com.acgist.snail.protocol.thunder.ThunderProtocol;
import com.acgist.snail.protocol.torrent.TorrentProtocol;

/**
 * <p>Snail下载工具</p>
 * <p>快速创建下载任务</p>
 * 
 * @author acgist
 * 
 * TODO：优化更多接口调用
 */
public final class Snail {

	/**
	 * <p>禁止创建实例</p>
	 */
	private Snail() {
	}
	
	/**
	 * <p>开始下载</p>
	 * 
	 * @param url 下载链接
	 * 
	 * @return 下载任务
	 * 
	 * @throws DownloadException 下载异常 
	 */
	public IDownloader download(String url) throws DownloadException {
		return DownloaderManager.getInstance().newTask(url);
	}

	/**
	 * <p>SnailBuilder</p>
	 * 
	 * @author acgist
	 */
	public static final class SnailBuilder {
		
		private static final SnailBuilder BUILDER = new SnailBuilder();
		
		/**
		 * <p>是否初始化任务</p>
		 */
		private boolean initTask = false;
		/**
		 * <p>是否初始化Torrent</p>
		 */
		private boolean initTorrent = false;
		/**
		 * <p>全局维护一个Snail对象</p>
		 */
		private Snail snail;
		
		/**
		 * <p>获取SnailBuilder</p>
		 * 
		 * @return SnailBuilder
		 */
		public static final SnailBuilder builder() {
			return BUILDER;
		}
		
		/**
		 * <p>禁止创建实例</p>
		 */
		private SnailBuilder() {
			EntityInitializer.newInstance().sync();
			ConfigInitializer.newInstance().sync();
		}

		/**
		 * <p>同步创建Snail</p>
		 * 
		 * @return Snail
		 */
		public Snail buildSync() {
			return this.build(true);
		}
		
		/**
		 * <p>异步创建Snail</p>
		 * 
		 * @return Snail
		 */
		public Snail buildAsyn() {
			return this.build(false);
		}
		
		/**
		 * <p>创建Snail</p>
		 * 
		 * @param sync 是否同步初始化
		 * 
		 * @return Snail
		 * 
		 * @throws DownloadException 下载异常 
		 */
		public Snail build(boolean sync) {
			if(this.snail != null) {
				return snail;
			}
			ProtocolManager.getInstance().available(true);
			this.loadInitializers().forEach(initializer -> {
				if(sync) {
					initializer.sync();
				} else {
					initializer.asyn();
				}
			});
			this.snail = new Snail();
			return this.snail;
		}

		/**
		 * <p>加载初始化列表</p>
		 * 
		 * @return 初始化列表
		 */
		private List<Initializer> loadInitializers() {
			final List<Initializer> list = new ArrayList<>();
			if(this.initTorrent) {
				list.add(NatInitializer.newInstance());
				list.add(DhtInitializer.newInstance());
				list.add(TrackerInitializer.newInstance());
				list.add(TorrentInitializer.newInstance());
				list.add(LocalServiceDiscoveryInitializer.newInstance());
			}
			if(this.initTask) {
				list.add(DownloaderInitializer.newInstance());
			}
			return list;
		}
		
		/**
		 * <p>加载任务</p>
		 * 
		 * @return SnailBuilder
		 */
		public SnailBuilder loadTask() {
			this.initTask = true;
			return this;
		}
		
		/**
		 * <p>注册下载协议</p>
		 * 
		 * @param protocol 下载协议
		 * 
		 * @return SnailBuilder
		 */
		public SnailBuilder register(Protocol protocol) {
			ProtocolManager.getInstance().register(protocol);
			return this;
		}
		
		/**
		 * <p>注册FTP下载协议</p>
		 * 
		 * @return SnailBuilder
		 */
		public SnailBuilder enableFtp() {
			return this.register(FtpProtocol.getInstance());
		}
		
		/**
		 * <p>注册HLS下载协议</p>
		 * 
		 * @return SnailBuilder
		 */
		public SnailBuilder enableHls() {
			return this.register(HlsProtocol.getInstance());
		}
		
		/**
		 * <p>注册HTTP下载协议</p>
		 * 
		 * @return SnailBuilder
		 */
		public SnailBuilder enableHttp() {
			return this.register(HttpProtocol.getInstance());
		}
		
		/**
		 * <p>注册Magnet下载协议</p>
		 * 
		 * @return SnailBuilder
		 */
		public SnailBuilder enableMagnet() {
			this.initTorrent = true;
			return this.register(MagnetProtocol.getInstance());
		}
		
		/**
		 * <p>注册Thunder下载协议</p>
		 * 
		 * @return SnailBuilder
		 */
		public SnailBuilder enableThunder() {
			return this.register(ThunderProtocol.getInstance());
		}
		
		/**
		 * <p>注册Torrent下载协议</p>
		 * 
		 * @return SnailBuilder
		 */
		public SnailBuilder enableTorrent() {
			this.initTorrent = true;
			return this.register(TorrentProtocol.getInstance());
		}
		
		/**
		 * <p>注册所有协议</p>
		 * 
		 * @return SnailBuilder
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
