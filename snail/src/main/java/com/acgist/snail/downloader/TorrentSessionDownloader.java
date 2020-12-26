package com.acgist.snail.downloader;

import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.peer.PeerManager;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.magnet.MagnetBuilder;

/**
 * <p>BT任务下载器</p>
 * 
 * @author acgist
 */
public abstract class TorrentSessionDownloader extends MultifileDownloader {
	
	/**
	 * <p>BT任务信息</p>
	 */
	protected TorrentSession torrentSession;
	
	/**
	 * @param taskSession 任务信息
	 */
	protected TorrentSessionDownloader(ITaskSession taskSession) {
		super(taskSession);
	}
	
	@Override
	public void open() throws NetException, DownloadException {
		// 不能在构造函数中初始化：防止种子被删除后还能点击下载
		this.torrentSession = this.loadTorrentSession();
		super.open();
	}
	
	@Override
	public void delete() {
		// 删除任务信息
		if(this.torrentSession != null) {
			final String infoHashHex = this.torrentSession.infoHashHex();
			PeerManager.getInstance().remove(infoHashHex); // 删除Peer信息
			TorrentManager.getInstance().remove(infoHashHex); // 删除种子信息
		}
		super.delete();
	}
	
	/**
	 * <p>加载BT任务信息</p>
	 * 
	 * @return BT任务信息
	 * 
	 * @throws DownloadException 下载异常
	 */
	protected TorrentSession loadTorrentSession() throws DownloadException {
		final var torrentPath = this.taskSession.getTorrent();
		// 加载磁力链接信息
		final var magnet = MagnetBuilder.newInstance(this.taskSession.getUrl()).build();
		final var infoHashHex = magnet.getHash();
		return TorrentManager.getInstance().newTorrentSession(infoHashHex, torrentPath);
	}
	
	@Override
	protected boolean checkCompleted() {
		return this.torrentSession.checkCompleted();
	}

}
