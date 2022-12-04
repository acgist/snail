package com.acgist.snail.net.torrent.peer;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.IStatisticsSession;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.torrent.IPeerConnect;
import com.acgist.snail.net.torrent.TorrentPiece;
import com.acgist.snail.net.torrent.TorrentSession;
import com.acgist.snail.utils.BeanUtils;

/**
 * <p>Peer连接</p>
 * <p>连接：下载、上传（解除阻塞可以上传）</p>
 * <p>接入：上传、下载（解除阻塞可以下载）</p>
 * 
 * @author acgist
 */
public abstract class PeerConnect implements IPeerConnect {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PeerConnect.class);

	/**
	 * <p>SLICE请求数量：{@value}</p>
	 * <p>注意：过大会导致UTP信号量阻塞</p>
	 */
	private static final int SLICE_REQUEST_SIZE = 2;
	/**
	 * <p>SLICE最大请求数量：{@value}</p>
	 */
	private static final int SLICE_REQUEST_MAX_SIZE = 4;
	/**
	 * <p>SLICE请求等待时间（毫秒）：{@value}</p>
	 */
	private static final long SLICE_TIMEOUT = 10L * SystemConfig.ONE_SECOND_MILLIS;
	/**
	 * <p>PICEC完成等待时间（毫秒）：{@value}</p>
	 */
	private static final long COMPLETED_TIMEOUT = 30L * SystemConfig.ONE_SECOND_MILLIS;
	/**
	 * <p>释放等待时间（毫秒）：{@value}</p>
	 */
	private static final long RELEASE_TIMEOUT = 4L * SystemConfig.ONE_SECOND_MILLIS;
	
	/**
	 * <p>连接状态</p>
	 */
	protected volatile boolean available = false;
	/**
	 * <p>是否下载</p>
	 */
	private volatile boolean downloading = false;
	/**
	 * <p>当前下载Piece信息</p>
	 */
	private TorrentPiece downloadPiece;
	/**
	 * <p>SLICE锁</p>
	 * 
	 * @see #SLICE_TIMEOUT
	 * @see #SLICE_REQUEST_SIZE
	 * @see #SLICE_REQUEST_MAX_SIZE
	 */
	private final AtomicInteger sliceLock;
	/**
	 * <p>完成锁</p>
	 * 
	 * @see #COMPLETED_TIMEOUT
	 */
	private final AtomicBoolean completedLock;
	/**
	 * <p>释放锁</p>
	 * 
	 * @see #RELEASE_TIMEOUT
	 */
	private final AtomicBoolean releaseLock;
	/**
	 * <p>Peer信息</p>
	 */
	protected final PeerSession peerSession;
	/**
	 * <p>BT任务信息</p>
	 */
	protected final TorrentSession torrentSession;
	/**
	 * <p>统计信息</p>
	 */
	protected final IStatisticsSession statisticsSession;
	/**
	 * <p>Peer连接信息</p>
	 */
	protected final PeerConnectSession peerConnectSession;
	/**
	 * <p>Peer消息代理</p>
	 */
	protected final PeerSubMessageHandler peerSubMessageHandler;
	
	/**
	 * @param peerSession Peer信息
	 * @param torrentSession BT任务信息
	 * @param peerSubMessageHandler Peer消息代理
	 */
	protected PeerConnect(PeerSession peerSession, TorrentSession torrentSession, PeerSubMessageHandler peerSubMessageHandler) {
		this.sliceLock = new AtomicInteger(0);
		this.completedLock = new AtomicBoolean(false);
		this.releaseLock = new AtomicBoolean(false);
		this.peerSession = peerSession;
		this.statisticsSession = peerSession.statistics();
		this.torrentSession = torrentSession;
		this.peerConnectSession = new PeerConnectSession();
		this.peerSubMessageHandler = peerSubMessageHandler;
	}

	/**
	 * <p>获取Peer信息</p>
	 * 
	 * @return Peer信息
	 */
	public final PeerSession peerSession() {
		return this.peerSession;
	}
	
	/**
	 * <p>获取BT任务信息</p>
	 * 
	 * @return BT任务信息
	 */
	public final TorrentSession torrentSession() {
		return this.torrentSession;
	}
	
	/**
	 * <p>获取Peer连接信息</p>
	 * 
	 * @return Peer连接信息
	 */
	public final PeerConnectSession peerConnectSession() {
		return this.peerConnectSession;
	}
	
	@Override
	public final IPeerConnect.ConnectType connectType() {
		return this.peerSubMessageHandler.connectType();
	}
	
	/**
	 * <p>发送have消息</p>
	 * 
	 * @param indexArray Piece索引
	 * 
	 * @see PeerSubMessageHandler#have(Integer...)
	 */
	public final void have(Integer ... indexArray) {
		this.peerSubMessageHandler.have(indexArray);
	}
	
	/**
	 * <p>发送PEX消息</p>
	 * 
	 * @param bytes PEX消息
	 * 
	 * @see PeerSubMessageHandler#pex(byte[])
	 */
	public final void pex(byte[] bytes) {
		this.peerSubMessageHandler.pex(bytes);
	}
	
	/**
	 * <p>发送holepunch消息-rendezvous</p>
	 * 
	 * @param peerSession peerSession
	 * 
	 * @see PeerSubMessageHandler#holepunchRendezvous(PeerSession)
	 */
	public final void holepunchRendezvous(PeerSession peerSession) {
		this.peerSubMessageHandler.holepunchRendezvous(peerSession);
	}
	
	/**
	 * <p>发送holepunch消息-connect</p>
	 * 
	 * @param host 目标地址
	 * @param port 目标端口
	 * 
	 * @see PeerSubMessageHandler#holepunchConnect(String, int)
	 */
	public final void holepunchConnect(String host, int port) {
		this.peerSubMessageHandler.holepunchConnect(host, port);
	}
	
	/**
	 * <p>发送uploadOnly消息</p>
	 * 
	 * @see PeerSubMessageHandler#uploadOnly()
	 */
	public final void uploadOnly() {
		this.peerSubMessageHandler.uploadOnly();
	}
	
	/**
	 * <p>判断是否可用</p>
	 * 
	 * @return 是否可用
	 */
	public final boolean available() {
		return this.available && this.peerSubMessageHandler.available();
	}
	
	/**
	 * <p>Peer上传计分</p>
	 * 
	 * @param buffer 上传大小
	 */
	public final void uploadMark(int buffer) {
		this.peerConnectSession.upload(buffer);
		this.statisticsSession.upload(buffer);
		this.statisticsSession.uploadLimit(buffer);
	}
	
	/**
	 * <p>获取Peer上传评分</p>
	 * 
	 * @return Peer上传评分
	 */
	public final long uploadMark() {
		return this.peerConnectSession.uploadMark();
	}
	
	/**
	 * <p>Peer下载计分</p>
	 * 
	 * @param buffer 下载大小
	 */
	public final void downloadMark(int buffer) {
		this.peerConnectSession.download(buffer);
		this.statisticsSession.downloadLimit(buffer);
	}
	
	/**
	 * <p>获取Peer下载评分</p>
	 * 
	 * @return Peer下载评分
	 */
	public final long downloadMark() {
		return this.peerConnectSession.downloadMark();
	}

	/**
	 * <p>开始下载</p>
	 */
	public void download() {
		if(!this.downloading) {
			synchronized (this) {
				if(!this.downloading) {
					this.downloading = true;
					this.torrentSession.submit(this::requests);
				}
			}
		}
	}
	
	/**
	 * <p>保存Piece数据</p>
	 * 
	 * @param index Piece索引
	 * @param begin Piece偏移
	 * @param bytes Piece数据
	 */
	public final void piece(int index, int begin, byte[] bytes) {
		if(bytes == null || this.downloadPiece == null) {
			return;
		}
		final int downloadIndex = this.downloadPiece.getIndex();
		if(index != downloadIndex) {
			LOGGER.debug("下载Piece索引和当前Piece索引不符：{}-{}", index, downloadIndex);
			return;
		}
		// 释放slice锁
		this.unlockSlice();
		final boolean completed = this.downloadPiece.write(begin, bytes);
		// 下载完成：释放完成锁
		if(completed) {
			this.unlockCompleted();
		}
	}

	/**
	 * <p>释放资源</p>
	 * <p>释放下载、阻塞Peer、关闭Peer连接</p>
	 */
	public void release() {
		this.available = false;
		this.releaseDownload();
		if(this.peerSubMessageHandler.available()) {
			this.peerSubMessageHandler.choke();
		}
		this.peerSubMessageHandler.close();
	}
	
	/**
	 * <p>请求下载</p>
	 */
	private void requests() {
		LOGGER.debug("开始请求下载：{}", this.peerSession);
		boolean success = true;
		while(success) {
			try {
				success = this.request();
			} catch (Exception e) {
				LOGGER.error("Peer请求异常", e);
			}
		}
		this.completedLock.set(true);
		this.releaseDownload();
		this.torrentSession.checkCompletedAndDone();
		// 验证最后选择Piece是否下载完成
		if(this.downloadPiece != null && !this.downloadPiece.completedAndVerify()) {
			LOGGER.debug("Piece最后失败：{}", this.downloadPiece);
			this.torrentSession.undone(this.downloadPiece);
			this.peerSubMessageHandler.cancel(this.downloadPiece.getIndex(), this.downloadPiece.getBegin(), this.downloadPiece.getLength());
		}
		LOGGER.debug("结束请求下载：{}", this.peerSession);
	}
	
	/**
	 * <p>请求数据</p>
	 * 
	 * @return 是否可以继续下载
	 */
	private boolean request() {
		if(!this.available()) {
			return false;
		}
		if(!this.torrentSession.downloadable()) {
			LOGGER.debug("释放Peer：任务不可下载");
			return false;
		}
		this.pick();
		if(this.downloadPiece == null) {
			LOGGER.debug("释放Peer：没有匹配Piece下载");
			this.peerSubMessageHandler.notInterested();
			return false;
		}
		final int index = this.downloadPiece.getIndex();
		while(this.available()) {
			// 超过slice请求数量进入等待
			if (this.sliceLock.get() >= SLICE_REQUEST_SIZE) {
				this.lockSlice();
			}
			// 超过slice最大请求数量跳出循环
			if (this.sliceLock.get() >= SLICE_REQUEST_MAX_SIZE) {
				LOGGER.debug("超过slice最大请求数量跳出循环");
				break;
			}
			this.sliceLock.incrementAndGet();
			// 顺序不能调换：position、length
			final int begin = this.downloadPiece.position();
			final int length = this.downloadPiece.length();
			this.peerSubMessageHandler.request(index, begin, length);
			// 是否还有更多SLICE
			if(!this.downloadPiece.hasMoreSlice()) {
				break;
			}
		}
		// 添加完成锁：请求发送完成必须进入完成等待（数据可能没有全部返回）
		this.lockCompleted();
		// 释放释放锁
		this.unlockRelease();
		return true;
	}
	
	/**
	 * <p>选择下载Piece</p>
	 */
	private void pick() {
		if(this.downloadPiece == null) {
			// 没有Piece
		} else if(this.downloadPiece.completed()) {
			if(this.downloadPiece.verify()) {
				final boolean success = this.torrentSession.write(this.downloadPiece);
				if(success) {
					// 统计下载有效数据
					this.statisticsSession.download(this.downloadPiece.getLength());
				} else {
					LOGGER.debug("Piece保存失败：{}", this.downloadPiece);
					this.torrentSession.undone(this.downloadPiece);
				}
			} else {
				// 设置下载错误Piece位图
				this.peerSession.badPieces(this.downloadPiece.getIndex());
				LOGGER.warn("Piece校验失败：{}", this.downloadPiece);
				this.torrentSession.undone(this.downloadPiece);
			}
		} else {
			LOGGER.debug("Piece下载失败：{}", this.downloadPiece);
			this.torrentSession.undone(this.downloadPiece);
		}
		if(this.peerConnectSession.isPeerUnchoked()) {
			LOGGER.debug("选择下载Piece：解除阻塞");
			this.downloadPiece = this.torrentSession.pick(this.peerSession.availablePieces(), this.peerSession.suggestPieces());
		} else {
			LOGGER.debug("选择下载Piece：快速允许");
			this.downloadPiece = this.torrentSession.pick(this.peerSession.allowedPieces(), this.peerSession.allowedPieces());
		}
		LOGGER.debug("选择下载Piece：{}", this.downloadPiece);
		this.sliceLock.set(0);
		this.completedLock.set(false);
	}
	
	/**
	 * <p>PeerConnect释放下载</p>
	 */
	protected final void releaseDownload() {
		if(this.downloading) {
			LOGGER.debug("PeerConnect释放下载：{}", this.peerSession);
			this.downloading = false;
			// 没有完成：等待下载完成
			if(!this.completedLock.get()) {
				this.lockRelease();
			}
		}
	}
	
	/**
	 * <p>添加slice锁</p>
	 */
	private void lockSlice() {
		synchronized (this.sliceLock) {
			try {
				this.sliceLock.wait(SLICE_TIMEOUT);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				LOGGER.debug("线程等待异常", e);
			}
		}
	}
	
	/**
	 * <p>释放slice锁</p>
	 */
	private void unlockSlice() {
		if (this.sliceLock.decrementAndGet() <= 0) {
			synchronized (this.sliceLock) {
				this.sliceLock.notifyAll();
			}
		}
	}
	
	/**
	 * <p>添加完成锁</p>
	 */
	private void lockCompleted() {
		if(!this.completedLock.get()) {
			synchronized (this.completedLock) {
				if(!this.completedLock.get()) {
					try {
						this.completedLock.wait(COMPLETED_TIMEOUT);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						LOGGER.debug("线程等待异常", e);
					}
				}
			}
		}
	}
	
	/**
	 * <p>释放完成锁</p>
	 */
	private void unlockCompleted() {
		synchronized (this.completedLock) {
			this.completedLock.set(true);
			this.completedLock.notifyAll();
		}
	}

	/**
	 * <p>添加释放锁</p>
	 */
	private void lockRelease() {
		if(!this.releaseLock.get()) {
			synchronized (this.releaseLock) {
				if(!this.releaseLock.get()) {
					this.releaseLock.set(true);
					try {
						this.releaseLock.wait(RELEASE_TIMEOUT);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						LOGGER.debug("线程等待异常", e);
					}
				}
			}
		}
	}

	/**
	 * <p>释放释放锁</p>
	 */
	private void unlockRelease() {
		if(this.releaseLock.get()) {
			synchronized (this.releaseLock) {
				if(this.releaseLock.get()) {
					this.releaseLock.notifyAll();
				}
			}
		}
	}
	
	@Override
	public String toString() {
		return BeanUtils.toString(this, this.peerSession);
	}
	
}
