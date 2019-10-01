package com.acgist.snail.downloader.torrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.TorrentSessionDownloader;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerManager;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.magnet.bootstrap.MagnetReader;
import com.acgist.snail.system.exception.DownloadException;

/**
 * <p>BT下载器</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TorrentDownloader extends TorrentSessionDownloader {

	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentDownloader.class);
	
	private TorrentDownloader(TaskSession taskSession) {
		super(taskSession);
	}

	public static final TorrentDownloader newInstance(TaskSession taskSession) {
		return new TorrentDownloader(taskSession);
	}

	@Override
	public void release() {
		this.torrentSession.releaseDownload();
	}

	@Override
	public void delete() {
		// 释放BT资源
		if(this.torrentSession != null) {
			this.torrentSession.releaseUpload();
			final String infoHashHex = this.torrentSession.infoHashHex();
			PeerManager.getInstance().remove(infoHashHex);
			TorrentManager.getInstance().remove(infoHashHex);
		}
		super.delete();
	}
	
	/**
	 * {@inheritDoc}
	 * <p>加载完成立即使任务可以被分享</p>
	 */
	@Override
	protected TorrentSession loadTorrentSession() {
		final var entity = this.taskSession.entity();
		final var path = entity.getTorrent();
		try {
			final var magnet = MagnetReader.newInstance(entity.getUrl()).magnet();
			final var infoHashHex = magnet.getHash();
			final var torrentSession = TorrentManager.getInstance().newTorrentSession(infoHashHex, path);
			torrentSession.upload(this.taskSession);
			return torrentSession;
		} catch (DownloadException e) {
			fail("BT任务加载失败：" + e.getMessage());
			LOGGER.error("BT任务加载异常", e);
		}
		return null;
	}
	
	@Override
	protected void loadDownload() {
		try {
			this.complete = this.torrentSession.download();
		} catch (DownloadException e) {
			fail("BT任务加载失败：" + e.getMessage());
			LOGGER.error("BT任务加载异常", e);
		}
	}

}
