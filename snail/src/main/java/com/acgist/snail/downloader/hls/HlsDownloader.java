package com.acgist.snail.downloader.hls;

import com.acgist.snail.context.ITaskSession;
import com.acgist.snail.downloader.MultifileDownloader;
import com.acgist.snail.net.DownloadException;
import com.acgist.snail.net.NetException;
import com.acgist.snail.net.hls.HlsContext;
import com.acgist.snail.net.hls.HlsSession;
import com.acgist.snail.net.hls.TsLinker;

/**
 * HLS任务下载器
 * 
 * @author acgist
 */
public final class HlsDownloader extends MultifileDownloader {

	/**
	 * HLS任务信息
	 */
	private HlsSession hlsSession;
	
	/**
	 * @param taskSession 任务信息
	 */
	protected HlsDownloader(ITaskSession taskSession) {
		super(taskSession);
	}

	/**
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
				// 连接文件
				this.tsLink();
				// 下载完成没有存在分享所以直接删除资源
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
	 * 加载HLS任务信息
	 * 
	 * @return HLS任务信息
	 */
	private HlsSession loadHlsSession() {
		return HlsContext.getInstance().hlsSession(this.taskSession);
	}
	
	/**
	 * 连接文件
	 * 下载完成连接文件碎片
	 */
	private void tsLink() {
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
