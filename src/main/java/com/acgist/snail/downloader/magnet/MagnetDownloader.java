package com.acgist.snail.downloader.magnet;

import com.acgist.snail.downloader.TorrentSessionDownloader;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.system.exception.DownloadException;

/**
 * <p>磁力链接下载器</p>
 * <p>下载原理：先将磁力链接转为种子文件，然后将任务转为BT任务进行下载。</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class MagnetDownloader extends TorrentSessionDownloader {
	
//	private static final Logger LOGGER = LoggerFactory.getLogger(MagnetDownloader.class);

	public MagnetDownloader(ITaskSession taskSession) {
		super(taskSession);
	}
	
	/**
	 * <p>创建磁力链接下载器</p>
	 * 
	 * @param taskSession 任务信息
	 * 
	 * @return {@link MagnetDownloader}
	 */
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
	protected void loadDownload() throws DownloadException {
		if(this.torrentSession != null) {
			this.complete = this.torrentSession.magnet(this.taskSession);
		}
	}

}
