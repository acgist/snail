package com.acgist.snail.downloader.hls;

import com.acgist.snail.downloader.MultifileDownloader;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.system.exception.DownloadException;

/**
 * <p>HLS任务下载器</p>
 * 
 * @author acgist
 * @since 1.4.1
 */
public final class HlsDownloader extends MultifileDownloader {

	protected HlsDownloader(ITaskSession taskSession) {
		super(taskSession);
	}

	/**
	 * <p>创建HLS任务下载器</p>
	 * 
	 * @param taskSession 任务信息
	 * 
	 * @return {@link HlsDownloader	}
	 */
	public static final HlsDownloader newInstance(ITaskSession taskSession) {
		return new HlsDownloader(taskSession);
	}
	
	@Override
	protected void loadDownload() throws DownloadException {
		
	}
	
	@Override
	protected boolean checkCompleted() {
		// TODO：实现
		return true;
	}

}
