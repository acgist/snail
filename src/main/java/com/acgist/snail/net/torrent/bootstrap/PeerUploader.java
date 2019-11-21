package com.acgist.snail.net.torrent.bootstrap;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.peer.bootstrap.PeerSubMessageHandler;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.config.PeerConfig;

/**
 * <p>Peer接入</p>
 * <p>提供上传功能：上传数据</p>
 * <p>如果被解除阻塞也开始请求下载</p>
 * 
 * @author acgist
 * @since 1.0.2
 */
public final class PeerUploader extends PeerDownloader {

	private static final Logger LOGGER = LoggerFactory.getLogger(PeerUploader.class);
	
	/**
	 * Peer上传评分
	 */
	private final AtomicLong uploadMark = new AtomicLong(0);
	
	private PeerUploader(PeerSession peerSession, TorrentSession torrentSession, PeerSubMessageHandler peerSubMessageHandler) {
		super(peerSession, torrentSession, peerSubMessageHandler);
		this.available = true;
	}
	
	public static final PeerUploader newInstance(PeerSession peerSession, TorrentSession torrentSession, PeerSubMessageHandler peerSubMessageHandler) {
		return new PeerUploader(peerSession, torrentSession, peerSubMessageHandler);
	}

	/**
	 * <p>Peer上传评分</p>
	 * <p>评分=当前下载大小-上次下载大小</p>
	 */
	@Override
	public long uploadMark() {
		final long nowSize = this.peerSession.statistics().uploadSize();
		final long oldSize = this.uploadMark.getAndSet(nowSize);
		return nowSize - oldSize;
	}
	
	@Override
	public void download() {
		if(
			this.peerSession.isPeerUnchoked() || // 解除阻塞
			this.peerSession.supportAllowedFast() // 允许快速下载
		) {
			super.download();
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>释放下载、关闭Peer客户端、设置非上传状态</p>
	 */
	@Override
	public void releaseUpload() {
		try {
			if(this.available) {
				this.releaseDownload();
				LOGGER.debug("PeerUploader关闭：{}-{}", this.peerSession.host(), this.peerSession.port());
				this.available = false;
				this.peerSubMessageHandler.choke();
				this.peerSubMessageHandler.close();
			}
		} catch (Exception e) {
			LOGGER.error("PeerUploader关闭异常", e);
		} finally {
			this.peerSession.statusOff(PeerConfig.STATUS_UPLOAD);
			this.peerSession.peerUploader(null);
			this.peerSession.reset();
		}
	}
	
}
