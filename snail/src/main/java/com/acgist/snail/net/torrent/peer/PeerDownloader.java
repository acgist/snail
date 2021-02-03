package com.acgist.snail.net.torrent.peer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.PeerConfig;
import com.acgist.snail.net.torrent.utp.UtpClient;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TorrentSession;

/**
 * <p>Peer下载</p>
 * <p>主动连接Peer</p>
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
	 * <p>创建Peer下载</p>
	 * 
	 * @param peerSession Peer信息
	 * @param torrentSession BT任务信息
	 * 
	 * @return Peer下载
	 */
	public static final PeerDownloader newInstance(PeerSession peerSession, TorrentSession torrentSession) {
		return new PeerDownloader(peerSession, torrentSession);
	}
	
	/**
	 * <p>握手</p>
	 * <p>建立连接、发送握手</p>
	 * 
	 * @return 是否握手成功
	 */
	public boolean handshake() {
		final boolean success = this.connect();
		if(success) {
			this.peerSubMessageHandler.handshake(this);
		} else {
			this.peerSession.incrementFailTimes();
		}
		this.available = success;
		return success;
	}
	
	/**
	 * <p>建立连接</p>
	 * <p>优先直接使用TCP/UTP进行连接，如果连接失败使用holepunch协议重连。</p>
	 * 
	 * @return 是否连接成功
	 */
	private boolean connect() {
		if(this.peerSession.utp()) {
			LOGGER.debug("Peer连接（uTP）：{}-{}", this.peerSession.host(), this.peerSession.port());
			final UtpClient utpClient = UtpClient.newInstance(this.peerSession, this.peerSubMessageHandler);
			final boolean utpOk = utpClient.connect();
			if(utpOk) {
				return utpOk;
			} else {
				return this.holepunchConnect(false); // 不需要再使用UTP重试
			}
		} else {
			LOGGER.debug("Peer连接（TCP）：{}-{}", this.peerSession.host(), this.peerSession.port());
			final PeerClient peerClient = PeerClient.newInstance(this.peerSession, this.peerSubMessageHandler);
			final boolean tcpOk = peerClient.connect();
			if(tcpOk) {
				return tcpOk;
			} else {
				return this.holepunchConnect(true); // 需要使用UTP重试
			}
		}
	}

	/**
	 * <p>使用holepunch协议连接</p>
	 * <p>如果Peer不可以直接连接使用holepunch协议连接，如果连接失败并且可以使用UTP重试时再使用UTP重试连接。</p>
	 * 
	 * @param utpRetry 是否可以使用UTP重试
	 * 
	 * @return 是否连接成功
	 */
	private boolean holepunchConnect(boolean utpRetry) {
		// Peer不可以直接连接：使用holepunch协议连接
		if(!this.peerSession.outgo()) {
			final PeerSession pexSource = this.peerSession.pexSource(); // Pex来源：中继
			if(
				pexSource != null &&
				pexSource.holepunch() && // 中继支持holepunch
				pexSource.connected() && // 中继已经连接
				this.peerSession.holepunch() // 目标支持holepunch
			) {
				final var peerConnect = pexSource.peerConnect();
				if(peerConnect != null) {
					if(!this.peerSession.holeunchConnect()) { // 是否已经连接
						// 向中继发送rendezvous消息
						peerConnect.holepunchRendezvous(this.peerSession);
					}
					// 已经收到holepunch连接消息使用UTP协议连接
					if(this.peerSession.holeunchConnect()) {
						LOGGER.debug("Peer连接（uTP）（holepunch）：{}-{}", this.peerSession.host(), this.peerSession.port());
						final UtpClient utpClient = UtpClient.newInstance(this.peerSession, this.peerSubMessageHandler);
						return utpClient.connect();
					} else {
						LOGGER.debug("Peer连接（uTP）（holepunch）：连接失败");
					}
				}
			}
		}
		// holepunch协议连接失败并且可以使用UTP重试
		if(utpRetry) {
			LOGGER.debug("Peer连接（uTP）（重试）：{}-{}", this.peerSession.host(), this.peerSession.port());
			final UtpClient utpClient = UtpClient.newInstance(this.peerSession, this.peerSubMessageHandler);
			final boolean utpOk = utpClient.connect();
			if(utpOk) {
				this.peerSession.flags(PeerConfig.PEX_UTP); // UTP
				this.peerSession.flags(PeerConfig.PEX_OUTGO); // 直接连接
				return utpOk;
			}
		}
		return false;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>设置非下载状态</p>
	 */
	@Override
	public void release() {
		try {
			if(this.available) {
				LOGGER.debug("PeerDownloader关闭：{}-{}", this.peerSession.host(), this.peerSession.port());
				super.release();
			}
		} catch (Exception e) {
			LOGGER.error("PeerDownloader关闭异常", e);
		} finally {
			this.peerSession.statusOff(PeerConfig.STATUS_DOWNLOAD);
			this.peerSession.peerDownloader(null);
		}
	}

}
