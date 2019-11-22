package com.acgist.snail.net.torrent;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.peer.bootstrap.PeerEvaluator;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerEvaluator.Type;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerSubMessageHandler;
import com.acgist.snail.pojo.bean.TorrentPiece;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.utils.ObjectUtils;
import com.acgist.snail.utils.ThreadUtils;

/**
 * <p>Peer连接</p>
 * <p>连接：下载、上传（解除阻塞可以上传）</p>
 * <p>接入：上传、下载（解除阻塞可以下载）</p>
 * 
 * @author acgist
 * @since 1.1.1
 */
public abstract class PeerConnect {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PeerConnect.class);

	/**
	 * <p>Piece每次请求SLICE数量</p>
	 * <p>注：过大会导致UTP信号量阻塞</p>
	 * 
	 * TODO：根据速度优化
	 */
	private static final int SLICE_REQUEST_SIZE = 2;
	/**
	 * <p>最大等待请求最大数量</p>
	 * <p>超过这个数量直接跳出下载</p>
	 */
	private static final int MAX_WAIT_SLICE_REQUEST_SIZE = 4;
	/**
	 * SLICE请求等待时间
	 */
	private static final int SLICE_WAIT_TIME = 10;
	/**
	 * PICEC完成等待时间
	 */
	private static final int PIECE_WAIT_TIME = 30;
	/**
	 * 释放时等待时间
	 */
	private static final int RELEASE_WAIT_TIME = 4;
	
	/**
	 * <p>是否已被评分</p>
	 * <p>忽略首次获取评分：防止剔除</p>
	 */
	protected volatile boolean marked = false;
	/**
	 * 连接状态
	 */
	protected volatile boolean available = false;
	/**
	 * 是否下载
	 */
	private volatile boolean downloading = false;
	/**
	 * 当前下载Piece信息
	 */
	private TorrentPiece downloadPiece;
	/**
	 * Peer上传评分
	 */
	private final AtomicLong uploadMark = new AtomicLong(0);
	/**
	 * Peer下载评分
	 */
	private final AtomicLong downloadMark = new AtomicLong(0);
	/**
	 * Piece分片锁
	 */
	private final AtomicInteger countLock = new AtomicInteger(0);
	/**
	 * Peer释放锁
	 */
	private final AtomicBoolean releaseLock = new AtomicBoolean(false);
	/**
	 * Piece完成锁
	 */
	private final AtomicBoolean completeLock = new AtomicBoolean(false);
	
	protected final PeerSession peerSession;
	protected final TorrentSession torrentSession;
	protected final PeerSubMessageHandler peerSubMessageHandler;
	
	protected PeerConnect(PeerSession peerSession, TorrentSession torrentSession, PeerSubMessageHandler peerSubMessageHandler) {
		this.peerSession = peerSession;
		this.torrentSession = torrentSession;
		this.peerSubMessageHandler = peerSubMessageHandler;
	}

	public final PeerSession peerSession() {
		return this.peerSession;
	}
	
	public final TorrentSession torrentSession() {
		return this.torrentSession;
	}
	
	/**
	 * 发送have消息
	 */
	public final void have(int index) {
		this.peerSubMessageHandler.have(index);
	}
	
	/**
	 * 发送pex消息
	 */
	public final void pex(byte[] bytes) {
		this.peerSubMessageHandler.pex(bytes);
	}
	
	/**
	 * 发送holepunch连接消息
	 * 
	 * @param host 目标地址
	 * @param port 目标端口
	 */
	public final void holepunchConnect(String host, int port) {
		this.peerSubMessageHandler.holepunchConnect(host, port);
	}
	
	/**
	 * 发送uploadOnly消息
	 */
	public final void uploadOnly() {
		this.peerSubMessageHandler.uploadOnly();
	}
	
	/**
	 * 是否可用
	 */
	public final boolean available() {
		return this.available && this.peerSubMessageHandler.available();
	}
	
	/**
	 * 是否评分
	 */
	public final boolean marked() {
		if(this.marked) {
			return this.marked;
		}
		this.marked = true;
		return false;
	}
	
	/**
	 * <p>Peer上传评分</p>
	 * <p>评分=当前下载大小-上次下载大小</p>
	 */
	public final long uploadMark() {
		final long nowSize = this.peerSession.statistics().uploadSize();
		final long oldSize = this.uploadMark.getAndSet(nowSize);
		return nowSize - oldSize;
	}
	
	/**
	 * <p>Peer下载评分</p>
	 * <p>评分=下载数据大小</p>
	 * <p>评分后清除数据：不累计分数</p>
	 */
	public final long downloadMark() {
		return this.downloadMark.getAndSet(0);
	}

	/**
	 * 计算评分：下载数据大小
	 */
	private void downloadMark(int buffer) {
		this.downloadMark.addAndGet(buffer);
	}
	
	/**
	 * 开始下载
	 */
	public void download() {
		if(!this.downloading) {
			synchronized (this) {
				if(!this.downloading) {
					this.downloading = true;
					this.torrentSession.submit(() -> {
						requests();
					});
				}
			}
		}
	}
	
	/**
	 * 保存Piece数据
	 * 
	 * @param index Piece索引
	 * @param begin Piece偏移
	 * @param bytes Piece数据
	 */
	public final void piece(int index, int begin, byte[] bytes) {
		// 数据不完整抛弃当前块：重新选择下载块
		if(bytes == null || this.downloadPiece == null) {
			return;
		}
		if(index != this.downloadPiece.getIndex()) {
			LOGGER.warn("下载Piece索引和当前Piece索引不符：{}-{}", index, this.downloadPiece.getIndex());
			return;
		}
		downloadMark(bytes.length); // 下载评分
		// 请求数据下载完成：唤醒下载等待
		synchronized (this.countLock) {
			if (this.countLock.addAndGet(-1) <= 0) {
				this.countLock.notifyAll();
			}
		}
		final boolean complete = this.downloadPiece.put(begin, bytes);
		// 唤醒下载完成等待
		if(complete) {
			synchronized (this.completeLock) {
				if (this.completeLock.getAndSet(true)) {
					this.completeLock.notifyAll();
				}
			}
		}
	}

	/**
	 * <p>释放资源</p>
	 * <p>释放下载、阻塞Peer、关闭Peer连接</p>
	 */
	public void release() {
		this.available = false;
		this.releaseDownload();
		this.peerSubMessageHandler.choke();
		this.peerSubMessageHandler.close();
	}
	
	/**
	 * <p>请求下载</p>
	 * <p>跳出请求循环：设置完成状态、释放Peer下载、完成检测</p>
	 */
	private void requests() {
		LOGGER.debug("开始请求下载：{}", this.peerSession);
		boolean ok = true;
		while(ok) {
			try {
				ok = request();
			} catch (Exception e) {
				LOGGER.error("Peer请求异常", e);
				ok = false;
			}
		}
		this.completeLock.set(true);
		this.releaseDownload();
		this.torrentSession.checkCompletedAndDone();
		if(this.downloadPiece != null && !this.downloadPiece.complete()) {
			this.undone();
		}
		LOGGER.debug("结束请求下载：{}", this.peerSession);
	}
	
	/**
	 * <p>请求数据</p>
	 * <p>每次发送{@link #SLICE_REQUEST_SIZE}个请求，然后进入等待，当全部数据响应后，又开始发送请求，直到Piece下载完成。</p>
	 * <p>请求发送完成后进入完成等待</p>
	 * <p>每次请求如果等待时间超过{@link #SLICE_WAIT_TIME}跳出下载</p>
	 * <p>如果最后Piece没有下载完成标记为失败</p>
	 * 
	 * @return 是否可以继续下载
	 */
	private boolean request() {
		if(!available()) {
			return false;
		}
		if(!this.torrentSession.downloadable()) {
			LOGGER.debug("释放Peer：任务不可下载");
			return false;
		}
		this.pick(); // 挑选Piece
		if(this.downloadPiece == null) {
			LOGGER.debug("释放Peer：没有匹配Piece下载");
			this.peerSubMessageHandler.notInterested(); // 发送不感兴趣消息
			return false;
		}
		final int index = this.downloadPiece.getIndex();
		final var sliceAwaitTime = Duration.ofSeconds(SLICE_WAIT_TIME);
		while(available()) {
			if (this.countLock.get() >= SLICE_REQUEST_SIZE) {
				synchronized (this.countLock) {
					ThreadUtils.wait(this.countLock, sliceAwaitTime);
					// 等待slice数量超过最大等待数量跳出循环
					if (this.countLock.get() >= MAX_WAIT_SLICE_REQUEST_SIZE) {
						LOGGER.debug("请求slice数量超过最大等待数量：{}-{}", this.downloadPiece.getIndex(), this.countLock.get());
						break;
					}
				}
			}
			this.countLock.addAndGet(1);
			final int begin = this.downloadPiece.position();
			final int length = this.downloadPiece.length(); // 顺序不能调换
			this.peerSubMessageHandler.request(index, begin, length);
			// 是否还有更多SLICE
			if(!this.downloadPiece.haveMoreSlice()) {
				break;
			}
		}
		/*
		 * <p>此处不论是否有数据返回都需要进行结束等待，防止数据小于{@link #SLICE_REQUEST_SIZE}个slice时直接跳出了slice wait（countLock）导致响应还没有收到就直接结束了。</p>
		 */
		synchronized (this.completeLock) {
			if(!this.completeLock.getAndSet(true)) {
				ThreadUtils.wait(this.completeLock, Duration.ofSeconds(PIECE_WAIT_TIME));
			}
		}
		// 如果已经释放：唤醒释放锁
		if(this.releaseLock.get()) {
			synchronized (this.releaseLock) {
				if(this.releaseLock.getAndSet(true)) {
					this.releaseLock.notifyAll();
				}
			}
		}
		return true;
	}
	
	/**
	 * 选择下载Piece
	 */
	private void pick() {
		// 校验Piece
		if(this.downloadPiece == null) { // 没有Piece
		} else if(this.downloadPiece.complete()) { // 下载完成
			// 验证数据
			if(this.downloadPiece.verify()) {
				// 保存数据
				final boolean ok = this.torrentSession.write(this.downloadPiece);
				if(ok) {
					// 统计下载数据
					this.peerSession.download(this.downloadPiece.getLength());
				} else {
					LOGGER.debug("Piece保存失败：{}", this.downloadPiece.getIndex());
					this.undone();
				}
			} else {
				LOGGER.warn("Piece校验失败：{}", this.downloadPiece.getIndex());
				this.peerSession.badPieces(this.downloadPiece.getIndex());
				this.undone();
			}
		} else { // Piece没有下载完成
			LOGGER.debug("Piece没有下载完成：{}", this.downloadPiece.getIndex());
			this.undone();
		}
		// 挑选Piece
		if(this.peerSession.isPeerUnchoked()) { // 解除阻塞
			LOGGER.debug("选择下载块：解除阻塞");
			this.downloadPiece = this.torrentSession.pick(this.peerSession.availablePieces(), this.peerSession.suggestPieces());
		} else { // 快速允许
			LOGGER.debug("选择下载块：快速允许");
			this.downloadPiece = this.torrentSession.pick(this.peerSession.allowedPieces(), this.peerSession.allowedPieces());
		}
		if(this.downloadPiece != null) {
			LOGGER.debug("选取Piece：{}-{}-{}", this.downloadPiece.getIndex(), this.downloadPiece.getBegin(), this.downloadPiece.getEnd());
		}
		// 设置状态
		this.countLock.set(0);
		this.completeLock.set(false);
	}
	
	/**
	 * <p>下载失败</p>
	 */
	private void undone() {
		LOGGER.debug("Piece下载失败：{}", this.downloadPiece.getIndex());
		this.torrentSession.undone(this.downloadPiece);
	}
	
	/**
	 * <p>释放下载资源</p>
	 */
	protected final void releaseDownload() {
		try {
			if(this.downloading) {
				LOGGER.debug("PeerConnect关闭：{}-{}", this.peerSession.host(), this.peerSession.port());
				this.downloading = false;
				// 没有完成：等待下载完成
				if(!this.completeLock.get()) {
					if(!this.releaseLock.get()) {
						synchronized (this.releaseLock) {
							if(!this.releaseLock.getAndSet(true)) {
								ThreadUtils.wait(this.releaseLock, Duration.ofSeconds(RELEASE_WAIT_TIME));
							}
						}
					}
				}
				// Peer评估
				PeerEvaluator.getInstance().score(this.peerSession, Type.DOWNLOAD);
			}
		} catch (Exception e) {
			LOGGER.error("PeerConnect关闭异常", e);
		}
	}
	
	@Override
	public String toString() {
		return ObjectUtils.toString(this, this.peerSession.host(), this.peerSession.port(), this.peerSession.dhtPort());
	}
	
}