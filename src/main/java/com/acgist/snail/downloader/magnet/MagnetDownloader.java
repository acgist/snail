package com.acgist.snail.downloader.magnet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.TorrentSessionDownloader;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.system.exception.DownloadException;

/**
 * <p>磁力链接下载器</p>
 * <p>原理：先将磁力链接转为种子，然后转为BT任务下载。</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class MagnetDownloader extends TorrentSessionDownloader {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MagnetDownloader.class);

	public MagnetDownloader(ITaskSession taskSession) {
		super(taskSession);
	}
	
	public static final MagnetDownloader newInstance(ITaskSession taskSession) {
		return new MagnetDownloader(taskSession);
	}
	
	@Override
	public void delete() {
		if(this.torrentSession != null) {
			this.torrentSession.releaseMagnet(); // 释放磁力链接资源
		}
		super.delete();
	}

	@Override
	public void release() {
		if(this.torrentSession != null) {
			this.torrentSession.releaseMagnet(); // 释放磁力链接资源
		}
		super.release();
	}
	
	@Override
	protected void loadDownload() {
		try {
			if(this.torrentSession != null) {
				this.complete = this.torrentSession.magnet(this.taskSession);
			}
		} catch (DownloadException e) {
			LOGGER.error("磁力链接任务加载异常", e);
			fail("磁力链接任务加载失败：" + e.getMessage());
		}
	}

}
