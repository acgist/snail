package com.acgist.snail;

import com.acgist.snail.context.exception.DownloadException;
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
	 * <p>加载下载任务</p>
	 * 
	 * @return Snail
	 * 
	 * @throws DownloadException 下载异常 
	 */
	public Snail loadTask() throws DownloadException {
		DownloaderInitializer.newInstance().sync();
		return this;
	}
	
	/**
	 * <p>SnailBuilder</p>
	 * 
	 * @author acgist
	 *
	 */
	public static final class SnailBuilder {
		
		/**
		 * <p>获取SnailBuilder</p>
		 * 
		 * @return SnailBuilder
		 */
		public static final SnailBuilder newBuilder() {
			EntityInitializer.newInstance().sync();
			ConfigInitializer.newInstance().sync();
			return new SnailBuilder();
		}
		
		/**
		 * <p>禁止创建实例</p>
		 */
		private SnailBuilder() {
		}

		/**
		 * <p>创建Snail</p>
		 * 
		 * @return Snail
		 * 
		 * @throws DownloadException 下载异常 
		 */
		public Snail build() throws DownloadException {
			ProtocolManager.getInstance().available(true);
			return new Snail();
		}
		
		/**
		 * <p>注册下载协议</p>
		 * 
		 * @param protocol 下载协议
		 * 
		 * @return SnailBuilder
		 */
		public SnailBuilder enable(Protocol protocol) {
			ProtocolManager.getInstance().register(protocol);
			return this;
		}
		
		/**
		 * <p>注册FTP下载协议</p>
		 * 
		 * @return SnailBuilder
		 */
		public SnailBuilder ftp() {
			return this.enable(FtpProtocol.getInstance());
		}
		
		/**
		 * <p>注册HLS下载协议</p>
		 * 
		 * @return SnailBuilder
		 */
		public SnailBuilder hls() {
			return this.enable(HlsProtocol.getInstance());
		}
		
		/**
		 * <p>注册HTTP下载协议</p>
		 * 
		 * @return SnailBuilder
		 */
		public SnailBuilder http() {
			return this.enable(HttpProtocol.getInstance());
		}
		
		/**
		 * <p>注册Magnet下载协议</p>
		 * 
		 * @return SnailBuilder
		 * 
		 * @see #torrent()
		 */
		public SnailBuilder magnet() {
			this.torrent(); // 注册Torrent下载协议
			return this.enable(MagnetProtocol.getInstance());
		}
		
		/**
		 * <p>注册Thunder下载协议</p>
		 * 
		 * @return SnailBuilder
		 */
		public SnailBuilder thunder() {
			return this.enable(ThunderProtocol.getInstance());
		}
		
		/**
		 * <p>注册Torrent下载协议</p>
		 * 
		 * @return SnailBuilder
		 */
		public SnailBuilder torrent() {
			NatInitializer.newInstance().sync();
			DhtInitializer.newInstance().sync();
			TrackerInitializer.newInstance().sync();
			TorrentInitializer.newInstance().sync();
			LocalServiceDiscoveryInitializer.newInstance().sync();
			return this.enable(TorrentProtocol.getInstance());
		}
		
		/**
		 * <p>注册所有协议</p>
		 * 
		 * @return SnailBuilder
		 */
		public SnailBuilder allProtocol() {
			return this
				.ftp()
				.hls()
				.http()
				.magnet()
				.thunder()
				.torrent();
		}
		
	}
	
}
