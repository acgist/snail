package com.acgist.snail.net.torrent.bootstrap;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.peer.bootstrap.PeerSubMessageHandler;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.context.SystemThreadContext;

/**
 * <p>Peer接入组：上传</p>
 * <dl>
 * 	<dt>对接入请求下载的PeerConnect管理优化</dt>
 * 	<dd>清除长时间没有请求的Peer。</dd>
 * 	<dd>不能超过最大分享连接数（如果接入的Peer为当前连接的Peer可以忽略连接数）。</dd>
 * </dl>
 * 
 * TODO：长时间无法删除问题
 * 
 * @author acgist
 * @since 1.0.2
 */
public class PeerConnectGroup {

	private static final Logger LOGGER = LoggerFactory.getLogger(PeerConnectGroup.class);
	
	/**
	 * PeerConnect队列
	 */
	private final BlockingQueue<PeerConnect> peerConnects;
	
	private PeerConnectGroup() {
		this.peerConnects = new LinkedBlockingQueue<>();
	}
	
	public static final PeerConnectGroup newInstance(TorrentSession torrentSession) {
		return new PeerConnectGroup();
	}
	
	/**
	 * <p>创建接入连接</p>
	 * <p>如果Peer当前提供下载，可以直接给予上传，否者将验证是否超过了连接的最大数量。</p>
	 */
	public PeerConnect newPeerConnect(PeerSession peerSession, PeerSubMessageHandler peerSubMessageHandler) {
		synchronized (this.peerConnects) {
			LOGGER.debug("Peer接入：{}-{}", peerSession.host(), peerSession.peerPort());
			if(!peerSession.downloading()) {
				if(this.peerConnects.size() >= SystemConfig.getPeerSize()) {
					LOGGER.debug("Peer接入数超过最大接入数量，拒绝连接：{}-{}", peerSession.host(), peerSession.peerPort());
					return null;
				}
			}
			final PeerConnect peerConnect = PeerConnect.newInstance(peerSession, peerSubMessageHandler);
			peerSession.status(PeerConfig.STATUS_UPLOAD);
			this.offer(peerConnect);
			return peerConnect;
		}
	}
	
	/**
	 * 优化PeerConnect
	 */
	public void optimize() {
		LOGGER.debug("优化PeerConnect");
		synchronized (this.peerConnects) {
			try {
				inferiorPeerConnects();
			} catch (Exception e) {
				LOGGER.error("优化PeerConnect异常", e);
			}
		}
	}
	
	/**
	 * <p>释放资源</p>
	 * <p>释放所有接入的PeerConnect。</p>
	 */
	public void release() {
		LOGGER.debug("释放PeerConnectGroup");
		synchronized (this.peerConnects) {
			this.peerConnects.forEach(connect -> {
				SystemThreadContext.submit(() -> {
					connect.release();
				});
			});
			this.peerConnects.clear();
		}
	}

	private void offer(PeerConnect peerConnect) {
		final var ok = this.peerConnects.offer(peerConnect);
		if(!ok) {
			LOGGER.warn("PeerConnect丢失：{}", peerConnect);
		}
	}
	
	/**
	 * <p>剔除无效接入</p>
	 * <ul>
	 * 	<li>连接不可用。</li>
	 * 	<li>长时间没有请求。</li>
	 * </ul>
	 * <p>剔除时设置为阻塞。</p>
	 * 
	 * TODO：删除多余的链接，不能超过最大连接数。
	 */
	private void inferiorPeerConnects() {
		final int size = this.peerConnects.size();
		int index = 0;
		PeerConnect tmp = null;
		while(true) {
			if(index++ >= size) {
				break;
			}
			tmp = this.peerConnects.poll();
			if(tmp == null) {
				break;
			}
			// 不可用直接剔除
			if(!tmp.available()) {
				inferiorPeerConnect(tmp);
				continue;
			}
			// 提供下载的Peer提供上传
			if(tmp.peerSession().downloading()) {
				this.offer(tmp);
				continue;
			}
			// 获取评分
			final long mark = tmp.mark();
			// 第一次评分忽略
			if(!tmp.marked()) {
				this.offer(tmp);
				continue;
			}
			if(mark == 0L) {
				inferiorPeerConnect(tmp);
			} else {
				this.offer(tmp);
			}
		}
	}
	
	private void inferiorPeerConnect(PeerConnect peerConnect) {
		if(peerConnect != null) {
			final PeerSession peerSession = peerConnect.peerSession();
			LOGGER.debug("剔除无效PeerConnect：{}-{}", peerSession.host(), peerSession.peerPort());
			peerConnect.release();
		}
	}
	
}
