package com.acgist.snail.downloader.torrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.downloader.TorrentSessionDownloader;
import com.acgist.snail.gui.GuiManager;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.ITaskSession.Status;
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
	 * <p>创建BT任务下载器</p>
	 * 
	 * @param taskSession 任务信息
	 * 
	 * @return {@link TorrentDownloader}
	 */
	public static final TorrentDownloader newInstance(ITaskSession taskSession) {
		return new TorrentDownloader(taskSession);
	}
	
	@Override
	public void delete() {
		if(this.torrentSession != null) {
			this.torrentSession.releaseUpload(); // 释放上传资源
			this.statistics.resetUploadSpeed(); // 重置上传速度
		}
		super.delete();
	}
	
	@Override
	public void refresh() {
		// 任务没有被加载：不用重新加载（开始下载自动加载）
		if(this.torrentSession == null) {
			LOGGER.debug("BT任务信息没有加载跳过刷新");
			// 任务已经完成不会再次加载：任务下载完成软件重启
			if(this.taskSession.complete()) {
				GuiManager.getInstance().alert("下载失败", "任务已经完成");
			}
			return;
		}
		// 重新加载任务
		final int fileCount = this.torrentSession.reload();
		// 新增下载文件并且任务已经下载完成：修改暂停状态（任务下载完成软件没有重启）
		if(fileCount > 0 && this.taskSession.complete()) {
			LOGGER.debug("新增下载文件：{}", this.name());
			this.taskSession.setStatus(Status.PAUSE);
			this.taskSession.setEndDate(null);
			this.taskSession.update();
		}
	}
	
	@Override
	public boolean verify() {
		if(this.torrentSession == null) {
			LOGGER.debug("BT任务信息没有加载跳过文件校验");
			return true;
		}
		final boolean verify = this.torrentSession.verify();
		if(verify) {
			LOGGER.debug("BT任务文件校验成功");
		} else {
			LOGGER.debug("BT任务文件校验失败：更新位图");
			this.updatePayload(); // 保存位图
		}
		return verify;
	}
	
	@Override
	public void release() {
		if(this.torrentSession != null) {
			this.torrentSession.releaseDownload(); // 释放下载资源
			this.statistics.resetDownloadSpeed(); // 重置下载速度
			this.updatePayload(); // 保存位图
		}
		super.release();
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>加载完成立即开启上传服务，直到任务删除或者软件关闭。</p>
	 */
	@Override
	protected TorrentSession loadTorrentSession() throws DownloadException {
		final var torrentSession = super.loadTorrentSession();
		if(torrentSession != null) {
			torrentSession.upload(this.taskSession);
		}
		return torrentSession;
	}
	
	@Override
	protected void loadDownload() throws DownloadException {
		if(this.torrentSession != null) {
			this.complete = this.torrentSession.download();
		}
	}
	
	/**
	 * <p>保存已下载Piece位图</p>
	 */
	private void updatePayload() {
		final byte[] payload = this.torrentSession.pieces().toByteArray();
		this.taskSession.setPayload(payload);
		this.taskSession.update();
	}
	
}
