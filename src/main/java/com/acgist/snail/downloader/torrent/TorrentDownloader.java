package com.acgist.snail.downloader.torrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.TorrentSessionDownloader;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.exception.DownloadException;

/**
 * <p>BT下载器</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class TorrentDownloader extends TorrentSessionDownloader {

	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentDownloader.class);
	
	private TorrentDownloader(ITaskSession taskSession) {
		super(taskSession);
	}

	/**
	 * <p>创建BT下载器</p>
	 * 
	 * @param taskSession 任务信息
	 * 
	 * @return BT下载器对象
	 */
	public static final TorrentDownloader newInstance(ITaskSession taskSession) {
		return new TorrentDownloader(taskSession);
	}
	
	@Override
	public void delete() {
		if(this.torrentSession != null) {
			this.torrentSession.releaseUpload(); // 释放上传资源
			this.statistics().resetUploadSpeed(); // 重置上传速度
		}
		super.delete();
	}
	
	@Override
	public void refresh() {
		// TODO：修改下载文件时修改TorrentStreamGroup
	}
	
	@Override
	public void release() {
		if(this.torrentSession != null) {
			this.torrentSession.releaseDownload(); // 释放下载资源
			this.statistics().resetDownloadSpeed(); // 重置下载速度
		}
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>加载完成立即开启上传服务，直到任务删除或者软件关闭。</p>
	 */
	@Override
	protected TorrentSession loadTorrentSession() {
		final var torrentSession = super.loadTorrentSession();
		if(torrentSession != null) {
			try {
				torrentSession.upload(this.taskSession);
			} catch (DownloadException e) {
				LOGGER.error("BT任务上传异常", e);
				fail("BT任务上传失败：" + e.getMessage());
			}
		}
		return torrentSession;
	}
	
	@Override
	protected void loadDownload() {
		try {
			if(this.torrentSession != null) {
				this.complete = this.torrentSession.download();
			}
		} catch (DownloadException e) {
			LOGGER.error("BT任务下载异常", e);
			fail("BT任务下载失败：" + e.getMessage());
		}
	}

}
