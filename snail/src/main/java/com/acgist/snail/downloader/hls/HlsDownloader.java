package com.acgist.snail.downloader.hls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.HlsContext;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.downloader.MultifileDownloader;
import com.acgist.snail.net.hls.TsLinker;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.session.HlsSession;

/**
 * <p>HLS任务下载器</p>
 * 
 * @author acgist
 */
public final class HlsDownloader extends MultifileDownloader {

	private static final Logger LOGGER = LoggerFactory.getLogger(HlsDownloader.class);
	
	/**
	 * <p>HLS任务信息</p>
	 */
	private HlsSession hlsSession;
	
	/**
	 * @param taskSession 任务信息
	 */
	protected HlsDownloader(ITaskSession taskSession) {
		super(taskSession);
	}

	/**
	 * <p>创建HLS任务下载器</p>
	 * 
	 * @param taskSession 任务信息
	 * 
	 * @return {@link HlsDownloader}
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
		if(this.hlsSession != null) {
			this.hlsSession.release();
			if(this.completed) {
				this.tsLink();
				this.delete();
			} else if(this.statusDelete()) {
				this.delete();
			}
		}
		super.release();
	}
	
	@Override
	public void delete() {
		super.delete();
		if(this.hlsSession != null) {
			this.hlsSession.delete();
		}
	}
	
	@Override
	protected void loadDownload() throws DownloadException {
		this.completed = this.hlsSession.download();
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
		return HlsContext.getInstance().hlsSession(this.taskSession);
	}
	
	/**
	 * <p>文件连接</p>
	 * <p>任务完成时连接TS文件</p>
	 */
	private void tsLink() {
		LOGGER.debug("HLS任务连接文件：{}", this.taskSession.getName());
		// 连接文件
		final TsLinker linker = TsLinker.newInstance(
			this.taskSession.getName(),
			this.taskSession.getFile(),
			this.hlsSession.cipher(),
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
