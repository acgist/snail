package com.acgist.snail.downloader.magnet;

import com.acgist.snail.context.ITaskSession;
import com.acgist.snail.downloader.TorrentSessionDownloader;
import com.acgist.snail.downloader.torrent.TorrentDownloader;
import com.acgist.snail.net.DownloadException;

/**
 * <p>磁力链接任务下载器</p>
 * <p>下载原理：先将磁力链接转为种子文件，然后转为{@link TorrentDownloader}进行下载。</p>
 * <p>下载完成不要删除任务信息：转为BT任务继续使用</p>
 * 
 * @author acgist
 */
public final class MagnetDownloader extends TorrentSessionDownloader {
	
	/**
	 * @param taskSession 任务信息
	 */
	private MagnetDownloader(ITaskSession taskSession) {
		super(taskSession);
	}
	
	/**
	 * <p>新建磁力链接任务下载器</p>
	 * 
	 * @param taskSession 任务信息
	 * 
	 * @return {@link MagnetDownloader}
	 */
	public static final MagnetDownloader newInstance(ITaskSession taskSession) {
		return new MagnetDownloader(taskSession);
	}

	@Override
	public void release() {
		if(this.torrentSession != null) {
			this.torrentSession.releaseMagnet();
		}
		super.release();
	}
	
	@Override
	public void delete() {
		super.delete();
		if(this.torrentSession != null) {
			this.torrentSession.delete();
		}
	}
	
	@Override
	protected void loadDownload() throws DownloadException {
		this.completed = this.torrentSession.magnet(this.taskSession);
	}

}
