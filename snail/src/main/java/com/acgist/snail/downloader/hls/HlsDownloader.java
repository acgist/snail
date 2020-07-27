package com.acgist.snail.downloader.hls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.MultifileDownloader;
import com.acgist.snail.net.hls.bootstrap.TsLinker;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.session.HlsSession;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.exception.NetException;

/**
 * <p>HLS任务下载器</p>
 * 
 * @author acgist
 * @since 1.4.1
 */
public final class HlsDownloader extends MultifileDownloader {

	private static final Logger LOGGER = LoggerFactory.getLogger(HlsDownloader.class);
	
	/**
	 * <p>HLS任务信息</p>
	 */
	private HlsSession hlsSession;
	
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
	public void open() throws NetException, DownloadException {
		this.hlsSession = this.loadHlsSession();
		super.open();
	}
	
	@Override
	public void release() {
		this.link();
		this.hlsSession.release();
		super.release();
	}
	
	@Override
	protected void loadDownload() throws DownloadException {
		this.hlsSession.download();
	}
	
	@Override
	protected boolean checkCompleted() {
		return this.hlsSession.checkCompleted();
	}

	/**
	 * <p>加载HLS任务信息</p>
	 * 
	 * @return HLS任务信息
	 */
	private HlsSession loadHlsSession() {
		return HlsSession.newInstance(this.taskSession);
	}
	
	/**
	 * <p>文件连接</p>
	 */
	private void link() {
		if(this.complete) {
			LOGGER.debug("HLS任务连接文件：{}", this.taskSession.getName());
			// 连接文件
			final TsLinker linker = TsLinker.newInstance(
				this.taskSession.getName(),
				this.taskSession.getFile(),
				this.taskSession.multifileSelected()
			);
			final long size = linker.link();
			// 重新设置文件大小
			if(size >= 0L && size != this.taskSession.getSize()) {
				this.taskSession.setSize(size);
				this.taskSession.update();
			}
		}
	}
	
}
