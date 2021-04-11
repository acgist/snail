package com.acgist.snail.downloader.torrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.GuiContext;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.downloader.TorrentSessionDownloader;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.session.TorrentSession;

/**
 * <p>BT任务下载器</p>
 * 
 * @author acgist
 */
public final class TorrentDownloader extends TorrentSessionDownloader {

	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentDownloader.class);
	
	/**
	 * @param taskSession 任务信息
	 */
	private TorrentDownloader(ITaskSession taskSession) {
		super(taskSession);
	}

	/**
	 * <p>新建BT任务下载器</p>
	 * 
	 * @param taskSession 任务信息
	 * 
	 * @return {@link TorrentDownloader}
	 */
	public static final TorrentDownloader newInstance(ITaskSession taskSession) {
		return new TorrentDownloader(taskSession);
	}
	
	@Override
	public void refresh() throws DownloadException {
		super.refresh();
		// 文件没有更改
		boolean unchange = true;
		if(this.torrentSession != null) {
			// 任务信息已经加载：重新加载下载文件信息
			unchange = this.torrentSession.reload() <= 0;
		} else if(this.statusCompleted()) {
			// 任务信息没有加载（软件重启）：任务完成加载任务信息
			unchange = false;
			this.torrentSession = this.loadTorrentSession();
		}
		// 如果任务没有完成修改数据开始下载自动加载任务
		if(this.statusCompleted()) {
			// 完成任务校验数据
			if(unchange) {
				// 没有新增文件
				GuiContext.getInstance().alert("下载成功", "任务已经完成下载");
			} else if(this.torrentSession.verify()) {
				// 文件校验成功
				GuiContext.getInstance().alert("下载成功", "任务已经完成下载");
			} else {
				// 文件校验失败
				this.taskSession.repause();
				GuiContext.getInstance().alert("修改成功", "重新开始下载任务");
			}
		} else if(this.torrentSession != null) {
			// 任务没有完成并且任务重启可能为空：开始下载自动加载
			// 没有完成：校验下载状态
			this.torrentSession.checkCompletedAndDone();
		}
	}
	
	@Override
	public boolean verify() throws DownloadException {
		// 优先验证文件是否存在
		boolean verify = super.verify();
		if(verify) {
			if(this.torrentSession == null) {
				// 任务信息没有加载
				this.torrentSession = this.loadTorrentSession();
			}
			// BT文件校验
			verify = this.torrentSession.verify();
			if(verify) {
				LOGGER.debug("BT任务文件校验成功");
			} else {
				LOGGER.debug("BT任务文件校验失败：更新位图");
				this.torrentSession.updatePieces(true);
			}
		}
		return verify;
	}
	
	@Override
	public void release() {
		// 不要删除任务信息：做种
		if(this.torrentSession != null) {
			this.torrentSession.releaseDownload();
			this.statistics.resetDownloadSpeed();
			this.torrentSession.updatePieces(true);
		}
		super.release();
	}
	
	@Override
	public void delete() {
		super.delete();
		if(this.torrentSession != null) {
			this.torrentSession.releaseUpload();
			this.statistics.resetUploadSpeed();
			this.torrentSession.delete();
		}
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>加载完成立即开启上传服务，直到任务删除或者软件关闭。</p>
	 */
	@Override
	protected TorrentSession loadTorrentSession() throws DownloadException {
		final var torrentSession = super.loadTorrentSession();
		torrentSession.upload(this.taskSession);
		return torrentSession;
	}
	
	@Override
	protected void loadDownload() throws DownloadException {
		this.completed = this.torrentSession.download();
	}
	
}
