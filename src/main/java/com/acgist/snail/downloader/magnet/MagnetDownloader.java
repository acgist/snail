package com.acgist.snail.downloader.magnet;

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
 * <p>磁力链接下载器</p>
 * <p>先将磁力链接转为种子，然后转为BT任务下载。</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class MagnetDownloader extends TorrentSessionDownloader {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MagnetDownloader.class);

	public MagnetDownloader(TaskSession taskSession) {
		super(taskSession);
	}
	
	public static final MagnetDownloader newInstance(TaskSession taskSession) {
		return new MagnetDownloader(taskSession);
	}

	@Override
	public void release() {
		this.torrentSession.releaseMagnet();
	}
	
	@Override
	public void delete() {
		// 释放磁力链接资源
		if(this.torrentSession != null) {
			this.torrentSession.releaseMagnet();
			final String infoHashHex = this.torrentSession.infoHashHex();
			PeerManager.getInstance().remove(infoHashHex);
			TorrentManager.getInstance().remove(infoHashHex);
		}
		super.delete();
	}
	
	@Override
	protected TorrentSession loadTorrentSession() {
		final var entity = this.taskSession.entity();
		final var path = entity.getTorrent();
		try {
			final var magnet = MagnetReader.newInstance(entity.getUrl()).magnet();
			final var infoHashHex = magnet.getHash();
			return TorrentManager.getInstance().newTorrentSession(infoHashHex, path);
		} catch (DownloadException e) {
			fail("磁力链接任务加载失败：" + e.getMessage());
			LOGGER.error("磁力链接任务加载异常", e);
		}
		return null;
	}
	
	@Override
	protected void loadDownload() {
		try {
			this.complete = this.torrentSession.magnet(this.taskSession);
		} catch (DownloadException e) {
			fail("磁力链接任务加载失败：" + e.getMessage());
			LOGGER.error("磁力链接任务加载异常", e);
		}
	}

}
