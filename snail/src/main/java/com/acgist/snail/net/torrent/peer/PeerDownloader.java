package com.acgist.snail.net.torrent.peer;

import com.acgist.snail.config.PeerConfig;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.torrent.TorrentSession;
import com.acgist.snail.net.torrent.utp.UtpClient;

/**
 * <p>Peer下载</p>
 * 
 * @author acgist
 */
public final class PeerDownloader extends PeerConnect {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PeerDownloader.class);
	
	/**
	 * @param peerSession Peer信息
	 * @param torrentSession BT任务信息
	 */
	private PeerDownloader(PeerSession peerSession, TorrentSession torrentSession) {
		super(peerSession, torrentSession, PeerSubMessageHandler.newInstance(peerSession, torrentSession));
	}
	
	/**
	 * <p>新建Peer下载</p>
	 * 
	 * @param peerSession Peer信息
	 * @param torrentSession BT任务信息
	 * 
	 * @return {@link PeerDownloader}
	 */
	public static final PeerDownloader newInstance(PeerSession peerSession, TorrentSession torrentSession) {
		return new PeerDownloader(peerSession, torrentSession);
	}
	
	/**
	 * <p>握手</p>
	 * 
	 * @return 是否握手成功
	 */
	public boolean handshake() {
		// 建立连接
		final boolean success = this.connect();
		if(success) {
			// 发送握手
			this.peerSubMessageHandler.initClient(this).handshake();
		} else {
			this.peerSession.incrementFailTimes();
		}
		this.available = success;
		return success;
	}
	
	/**
	 * <p>建立连接</p>
	 * <p>优先使用TCP/UTP进行连接，如果连接失败使用holepunch协议重连。</p>
	 * 
	 * @return 是否连接成功
	 */
	private boolean connect() {
		if(this.peerSession.utp()) {
			LOGGER.debug("Peer连接（UTP）：{}", this.peerSession);
			final UtpClient utpClient = UtpClient.newInstance(this.peerSession, this.peerSubMessageHandler);
			final boolean utpOk = utpClient.connect();
			if(utpOk) {
				return utpOk;
			} else {
				return this.holepunchConnect(false);
			}
		} else {
			LOGGER.debug("Peer连接（TCP）：{}", this.peerSession);
			final PeerClient peerClient = PeerClient.newInstance(this.peerSession, this.peerSubMessageHandler);
			final boolean tcpOk = peerClient.connect();
			if(tcpOk) {
				return tcpOk;
			} else {
				return this.holepunchConnect(true);
			}
		}
	}

	/**
	 * <p>使用holepunch协议连接</p>
	 * 
	 * @param utpRetry 是否可以使用UTP重试
	 * 
	 * @return 是否连接成功
	 */
	private boolean holepunchConnect(boolean utpRetry) {
		// Peer不可以直接连接：使用holepunch协议连接
		if(!this.peerSession.outgo()) {
			// 中继：PEX来源
			final PeerSession pexSource = this.peerSession.pexSource();
			if(
				pexSource != null &&
				// 中继已经连接
				pexSource.connected() &&
				// 中继支持holepunch
				pexSource.holepunch() &&
				// 目标支持holepunch
				this.peerSession.holepunch()
			) {
				final var peerConnect = pexSource.peerConnect();
				if(peerConnect != null) {
					// 是否已经连接
					if(!this.peerSession.holeunchConnect()) {
						peerConnect.holepunchRendezvous(this.peerSession);
					}
					// 已经收到holepunch连接消息使用UTP协议连接
					if(this.peerSession.holeunchConnect()) {
						LOGGER.debug("Peer连接（holepunch）：{}", this.peerSession);
						final UtpClient utpClient = UtpClient.newInstance(this.peerSession, this.peerSubMessageHandler);
						return utpClient.connect();
					} else {
						LOGGER.debug("Peer连接失败（holepunch）：{}", this.peerSession);
					}
				}
			}
		}
		// holepunch协议连接失败并且可以使用UTP重试
		if(utpRetry) {
			LOGGER.debug("Peer连接重试（UTP）：{}", this.peerSession);
			final UtpClient utpClient = UtpClient.newInstance(this.peerSession, this.peerSubMessageHandler);
			final boolean utpOk = utpClient.connect();
			if(utpOk) {
				// 支持UTP
				this.peerSession.flags(PeerConfig.PEX_UTP);
				// 直接连接
				this.peerSession.flags(PeerConfig.PEX_OUTGO);
				return utpOk;
			}
		}
		return false;
	}
	
	@Override
	public void release() {
		try {
			if(this.available) {
				LOGGER.debug("关闭PeerDownloader：{}", this.peerSession);
				super.release();
			}
		} catch (Exception e) {
			LOGGER.error("关闭PeerDownloader异常", e);
		} finally {
			this.peerSession.statusOff(PeerConfig.STATUS_DOWNLOAD);
			this.peerSession.peerDownloader(null);
		}
	}

}
