package com.acgist.snail.downloader;

import java.io.IOException;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerManager;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.magnet.bootstrap.MagnetBuilder;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.ThreadUtils;

/**
 * <p>BT任务下载器</p>
 * 
 * @author acgist
 * @since 1.1.1
 */
public abstract class TorrentSessionDownloader extends Downloader {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentSessionDownloader.class);

	/**
	 * <p>BT任务信息</p>
	 */
	protected TorrentSession torrentSession;
	/**
	 * <p>下载锁</p>
	 * <p>下载时阻塞下载线程</p>
	 */
	protected final Object downloadLock = new Object();
	
	protected TorrentSessionDownloader(ITaskSession taskSession) {
		super(taskSession);
	}
	
	@Override
	public void open() {
		// 不能在构造函数中初始化：防止种子被删除后还能点击下载
		this.torrentSession = this.loadTorrentSession();
		loadDownload();
	}
	
	@Override
	public void delete() {
		// 删除任务信息
		if(this.torrentSession != null) {
			final String infoHashHex = this.torrentSession.infoHashHex();
			PeerManager.getInstance().remove(infoHashHex); // Peer信息
			TorrentManager.getInstance().remove(infoHashHex); // 种子信息
		}
		super.delete();
	}
	
	@Override
	public void download() throws IOException {
		while(ok()) {
			synchronized (this.downloadLock) {
				ThreadUtils.wait(this.downloadLock, Duration.ofSeconds(Integer.MAX_VALUE));
				this.complete = this.torrentSession.checkCompleted();
			}
		}
	}
	
	@Override
	public void unlockDownload() {
		synchronized (this.downloadLock) {
			this.downloadLock.notifyAll();
		}
	}
	
	/**
	 * <p>加载BT任务信息</p>
	 * 
	 * @return 任务信息
	 */
	protected TorrentSession loadTorrentSession() {
		final var torrentPath = this.taskSession.getTorrent();
		try {
			// 加载磁力链接信息
			final var magnet = MagnetBuilder.newInstance(this.taskSession.getUrl()).build();
			final var infoHashHex = magnet.getHash();
			return TorrentManager.getInstance().newTorrentSession(infoHashHex, torrentPath);
		} catch (DownloadException e) {
			LOGGER.error("BT任务加载异常", e);
			fail("BT任务加载失败：" + e.getMessage());
		}
		return null;
	}

	/**
	 * <p>开始下载</p>
	 */
	protected abstract void loadDownload();
	
}
