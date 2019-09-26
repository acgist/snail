package com.acgist.snail.downloader.magnet;

import java.io.IOException;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.Downloader;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerManager;
import com.acgist.snail.pojo.bean.Magnet;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.magnet.bootstrap.MagnetReader;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.ThreadUtils;

/**
 * <p>磁力链接下载器</p>
 * <p>先将磁力链接转为种子，然后转为BT任务下载。</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class MagnetDownloader extends Downloader {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MagnetDownloader.class);

	private TorrentSession torrentSession;
	/**
	 * 下载锁
	 */
	private final Object downloadLock = new Object();
	
	public MagnetDownloader(TaskSession taskSession) {
		super(taskSession);
		loadTorrent();
	}
	
	public static final MagnetDownloader newInstance(TaskSession taskSession) {
		return new MagnetDownloader(taskSession);
	}

	@Override
	public void open() {
		loadMagnet();
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
	public void release() {
		this.torrentSession.releaseMagnet();
	}
	
	@Override
	public void delete() {
		if(this.torrentSession != null) {
			this.torrentSession.releaseMagnet();
			final String infoHashHex = this.torrentSession.infoHashHex();
			PeerManager.getInstance().remove(infoHashHex);
			TorrentManager.getInstance().remove(infoHashHex);
		}
		super.delete();
	}
	
	@Override
	public void unlockDownload() {
		synchronized (this.downloadLock) {
			this.downloadLock.notifyAll();
		}
	}
	
	/**
	 * <p>加载任务</p>
	 * <p>创建时立即加载任务，使任务可以被分享。</p>
	 */
	private void loadTorrent() {
		final var entity = this.taskSession.entity();
		final String path = entity.getTorrent();
		try {
			final Magnet magnet = MagnetReader.newInstance(entity.getUrl()).magnet();
			final String infoHashHex = magnet.getHash();
			this.torrentSession = TorrentManager.getInstance().newTorrentSession(infoHashHex, path);
		} catch (DownloadException e) {
			fail("任务加载失败");
			LOGGER.error("任务加载异常", e);
		}
	}
	
	/**
	 * 开始磁力链接下载
	 */
	private void loadMagnet() {
		try {
			this.complete = this.torrentSession.magnet(this.taskSession);
		} catch (DownloadException e) {
			fail("任务加载失败");
			LOGGER.error("任务加载异常", e);
		}
	}

}
