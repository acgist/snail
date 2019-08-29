package com.acgist.snail.net.torrent.bootstrap;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.peer.PeerClient;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerSubMessageHandler;
import com.acgist.snail.net.torrent.utp.UtpClient;
import com.acgist.snail.pojo.bean.TorrentPiece;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.evaluation.PeerEvaluator;
import com.acgist.snail.utils.ObjectUtils;
import com.acgist.snail.utils.ThreadUtils;

/**
 * <p>Peer下载</p>
 * <p>提供下载功能，根据是否支持UTP选择使用UTP还是TCP。</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class PeerLauncher {

	/**
	 * 每批请求的SLICE数量
	 */
	private static final int SLICE_REQUEST_SIZE = 4;
	/**
	 * 每批SLICE请求等待时间
	 */
	private static final int SLICE_AWAIT_TIME = 20;
	/**
	 * 一个PICEC完成等待时间
	 */
	private static final int PIECE_AWAIT_TIME = 40;
	/**
	 * 释放时等待时间
	 */
	private static final int CLOSE_AWAIT_TIME = 4;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PeerLauncher.class);
	
	private volatile boolean launcher = false; // 是否启动
	private volatile boolean available = false; // 状态：连接是否成功
	private volatile boolean havePieceMessage = false; // 状态：是否返回数据
	
	private TorrentPiece downloadPiece; // 下载的Piece信息
	
	private final AtomicInteger countLock = new AtomicInteger(0); // Piece分片锁
	private final AtomicBoolean releaseLock = new AtomicBoolean(false); // 释放锁
	private final AtomicBoolean completeLock = new AtomicBoolean(false); // Piece完成锁
	
	/**
	 * 评分：每次收到Piece更新该值，评分时清零。
	 */
	private final AtomicInteger mark = new AtomicInteger(0);
	
	private final PeerSession peerSession;
	private final TorrentSession torrentSession;
	private final TorrentStreamGroup torrentStreamGroup;
	private final PeerSubMessageHandler peerSubMessageHandler;
	
	private PeerLauncher(PeerSession peerSession, TorrentSession torrentSession) {
		this.peerSession = peerSession;
		this.torrentSession = torrentSession;
		this.torrentStreamGroup = torrentSession.torrentStreamGroup();
		this.peerSubMessageHandler = PeerSubMessageHandler.newInstance(peerSession, torrentSession);
	}
	
	public static final PeerLauncher newInstance(PeerSession peerSession, TorrentSession torrentSession) {
		return new PeerLauncher(peerSession, torrentSession);
	}

	public PeerSession peerSession() {
		return this.peerSession;
	}

	/**
	 * 开始下载：发送请求
	 */
	public void download() {
		if(!this.launcher) {
			synchronized (this) {
				if(!this.launcher) {
					this.launcher = true;
					this.torrentSession.submit(() -> {
						requests();
					});
				}
			}
		}
	}

	/**
	 * 建立连接、发送握手
	 */
	public boolean handshake() {
		LOGGER.debug("Peer连接：{}-{}", this.peerSession.host(), this.peerSession.peerPort());
		final boolean ok = connect();
		if(ok) {
			this.peerSubMessageHandler.handshake(this);
			PeerEvaluator.getInstance().score(this.peerSession, PeerEvaluator.Type.connect);
		} else {
			this.peerSession.fail();
		}
		this.available = ok;
		return ok;
	}
	
	/**
	 * 连接
	 */
	private boolean connect() {
		if(this.peerSession.utp()) {
			final UtpClient utpClient = UtpClient.newInstance(this.peerSession, this.peerSubMessageHandler);
			return utpClient.connect();
		} else {
			final PeerClient peerClient = PeerClient.newInstance(this.peerSession, this.peerSubMessageHandler);
			return peerClient.connect();
		}
	}
	
	/**
	 * 发送have消息
	 */
	public void have(int index) {
		this.peerSubMessageHandler.have(index);
	}
	
	/**
	 * 发送Pex消息
	 */
	public void exchange(byte[] bytes) {
		this.peerSubMessageHandler.exchange(bytes);
	}
	
	/**
	 * 下载数据
	 */
	public void piece(int index, int begin, byte[] bytes) {
		if(bytes == null) { // 数据不完整抛弃当前块，重新选择下载块
			undone();
			return;
		}
		if(index != this.downloadPiece.getIndex()) {
			LOGGER.warn("下载Piece索引不符：{}-{}", index, this.downloadPiece.getIndex());
			return;
		}
		this.havePieceMessage = true;
		mark(bytes.length); // 每次获取到都需要打分
		synchronized (this.countLock) { // 唤醒request
			if (this.countLock.addAndGet(-1) <= 0) {
				this.countLock.notifyAll();
			}
		}
		final boolean complete = this.downloadPiece.put(begin, bytes); // 下载完成
		if(complete) { // 唤醒下载完成
			synchronized (this.completeLock) {
				if (this.completeLock.getAndSet(true)) {
					this.completeLock.notifyAll();
				}
			}
		}
	}
	
	/**
	 * 资源释放
	 */
	public void release() {
		try {
			if(this.available) {
				LOGGER.debug("PeerLauncher关闭：{}-{}", this.peerSession.host(), this.peerSession.peerPort());
				this.available = false;
				if(!this.completeLock.get()) { // 没有完成：等待下载完成
					if(!this.releaseLock.get()) {
						synchronized (this.releaseLock) {
							if(!this.releaseLock.getAndSet(true)) {
								ThreadUtils.wait(this.releaseLock, Duration.ofSeconds(CLOSE_AWAIT_TIME));
							}
						}
					}
				}
				this.peerSubMessageHandler.close();
			}
		} catch (Exception e) {
			LOGGER.error("PeerLauncher关闭异常", e);
		} finally {
			this.peerSession.unstatus(PeerConfig.STATUS_DOWNLOAD);
			this.peerSession.peerLauncher(null);
		}
	}
	
	/**
	 * 是否可用，Peer优化时如果有不可用的直接剔除
	 */
	public boolean available() {
		return this.available && this.peerSubMessageHandler.available();
	}
	
	/**
	 * <p>获取评分</p>
	 * <p>每次获取后清空，调用后清空重新开始计算。</p>
	 */
	public int mark() {
		return this.mark.getAndSet(0);
	}

	/**
	 * 下载失败
	 */
	private void undone() {
		LOGGER.debug("Piece下载失败：{}", this.downloadPiece.getIndex());
		this.torrentStreamGroup.undone(this.downloadPiece);
	}

	/**
	 * 一直进行下载直到结束
	 */
	private void requests() {
		boolean ok = true;
		while(ok) {
			ok = request();
		}
	}
	
	/**
	 * <p>请求数据</p>
	 * <p>每次发送{@link #SLICE_REQUEST_SIZE}个请求，进入等待，当全部数据响应后，又开始发送请求，直到Piece下载完成。</p>
	 * <p>请求发送完成后进入完成等待。</p>
	 * <p>如果等待时间超过{@link #SLICE_AWAIT_TIME}跳出下载。</p>
	 * <p>如果最后Piece没有下载完成标记为失败。</p>
	 * 
	 * @return 是否可以继续下载
	 */
	private boolean request() {
		if(!available()) {
			return false;
		}
		pickDownloadPiece();
		if(this.downloadPiece == null) {
			LOGGER.debug("没有匹配Peer块下载");
			this.peerSubMessageHandler.notInterested(); // 发送不感兴趣消息
			this.completeLock.set(true); // 没有匹配到下载块时设置为完成
			this.release();
			this.torrentSession.completeCheck();
			return false;
		}
		final int index = this.downloadPiece.getIndex();
		while(available()) {
			synchronized (this.countLock) {
				if (this.countLock.get() >= SLICE_REQUEST_SIZE) {
					ThreadUtils.wait(this.countLock, Duration.ofSeconds(SLICE_AWAIT_TIME));
					if (!this.havePieceMessage) { // 没有数据返回
						break;
					}
				}
				this.countLock.addAndGet(1);
			}
			int begin = this.downloadPiece.position();
			int length = this.downloadPiece.length(); // 顺序不能调换
			this.peerSubMessageHandler.request(index, begin, length);
			if(!this.downloadPiece.more()) { // 是否还有更多SLICE
				break;
			}
		}
		/*
		 * 此处不论是否有数据返回都需要进行结束等待，防止数据刚刚只有一个slice时直接跳出了slice wait（countLock）导致响应还没有收到就直接结束了。
		 * 设置true，下载完成时唤醒。
		 */
		synchronized (this.completeLock) {
			if(!this.completeLock.getAndSet(true)) {
				ThreadUtils.wait(this.completeLock, Duration.ofSeconds(PIECE_AWAIT_TIME));
			}
		}
		if(this.releaseLock.get()) { // 已经关闭
			synchronized (this.releaseLock) {
				this.releaseLock.notifyAll();
			}
		}
		if(this.countLock.get() > 0) { // 没有下载完成
			undone();
		}
		return true;
	}

	/**
	 * 选择下载Piece
	 */
	private void pickDownloadPiece() {
		if(!available()) { // 不可用
			return;
		}
		if(this.downloadPiece == null) { // 没有Piece
		} else if(this.downloadPiece.complete()) { // 完成
			if(this.downloadPiece.verify()) { // 验证数据
				final boolean ok = this.torrentStreamGroup.piece(this.downloadPiece); // 保存数据
				if(ok) {
					this.peerSession.download(this.downloadPiece.getLength()); // 统计
				}
			} else {
				this.peerSession.badPieces(this.downloadPiece.getIndex());
				LOGGER.warn("下载的Piece校验失败：{}", this.downloadPiece.getIndex());
			}
		} else { // 上次选择的Piece没有完成
			LOGGER.debug("上次选择Piece未下载完成：{}", this.downloadPiece.getIndex());
		}
		this.downloadPiece = this.torrentStreamGroup.pick(this.peerSession.availablePieces());
		if(this.downloadPiece != null) {
			LOGGER.debug("选取Piece：{}-{}-{}", this.downloadPiece.getIndex(), this.downloadPiece.getBegin(), this.downloadPiece.getEnd());
		}
		this.completeLock.set(false);
		this.countLock.set(0);
		this.havePieceMessage = false;
	}
	
	/**
	 * 计算评分：下载速度
	 */
	private void mark(int buffer) {
		this.mark.addAndGet(buffer); // 计算评分
	}

	@Override
	public String toString() {
		return ObjectUtils.toString(this, this.peerSession.host(), this.peerSession.peerPort(), this.peerSession.dhtPort());
	}

}
