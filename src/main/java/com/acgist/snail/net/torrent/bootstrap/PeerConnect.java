package com.acgist.snail.net.torrent.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.PeerClientHandler;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerSubMessageHandler;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.system.config.PeerConfig;

/**
 * <p>Peer接入</p>
 * <p>提供分享上传功能</p>
 * 
 * @author acgist
 * @since 1.0.2
 */
public final class PeerConnect extends PeerClientHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(PeerConnect.class);
	
	private PeerConnect(PeerSession peerSession, PeerSubMessageHandler peerSubMessageHandler) {
		super(peerSession, peerSubMessageHandler);
		this.available = true;
	}
	
	public static final PeerConnect newInstance(PeerSession peerSession, PeerSubMessageHandler peerSubMessageHandler) {
		return new PeerConnect(peerSession, peerSubMessageHandler);
	}

	/**
	 * {@inheritDoc}
	 * <p>使用当前下载大小减去旧记录，然后使用当前下载大小覆盖旧记录。</p>
	 */
	@Override
	public long mark() {
		final long nowSize = this.peerSession.statistics().uploadSize();
		final long oldSize = this.mark.getAndSet(nowSize);
		return nowSize - oldSize;
	}

	/**
	 * 释放资源：关闭Peer客户端，设置非上传状态。
	 */
	public void release() {
		try {
			if(this.available) {
				LOGGER.debug("PeerConnect关闭：{}-{}", this.peerSession.host(), this.peerSession.port());
				this.available = false;
				this.peerSubMessageHandler.choke();
				this.peerSubMessageHandler.close();
			}
		} catch (Exception e) {
			LOGGER.error("PeerConnect关闭异常", e);
		} finally {
			this.peerSession.statusOff(PeerConfig.STATUS_UPLOAD);
			this.peerSession.peerConnect(null);
			this.peerSession.reset();
		}
	}
	
}
