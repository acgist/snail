package com.acgist.snail.net.peer;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.torrent.bootstrap.TorrentStreamGroup;
import com.acgist.snail.net.TcpClient;
import com.acgist.snail.pojo.bean.TorrentPiece;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.utils.ThreadUtils;

/**
 * <p>Peer客户端</p>
 * <p>基本协议：TCP</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class PeerClient extends TcpClient<PeerMessageHandler> {
	
	private static final int SLICE_MAX_SIZE = 10; // 单次请求10个SLICE
	
	private static final int SLICE_AWAIT_TIME = 30; // SLICE每批等待时间
	private static final int PIECE_AWAIT_TIME = 60; // PIECE完成等待时间
	private static final int CLOSE_AWAIT_TIME = 60; // 关闭等待时间
	
//	private static final int SLICE_AWAIT_TIME = 4; // SLICE每批等待时间
//	private static final int PIECE_AWAIT_TIME = 4; // PIECE完成等待时间

	private static final Logger LOGGER = LoggerFactory.getLogger(PeerClient.class);
	
	private boolean launcher = false; // 是否启动
	private volatile boolean available = false; // 状态：连接是否成功
	private volatile boolean havePieceMessage = false; // 状态：是否返回数据
	
	private TorrentPiece downloadPiece; // 下载的Piece信息
	
	private Object closeLock = new Object(); // 关闭锁
	private AtomicInteger countLock = new AtomicInteger(0); // Piece分片锁
	private AtomicBoolean completeLock = new AtomicBoolean(false); // Piece完成锁
	
	/**
	 * 评分：每次收到Piece更新该值，评分时清零。
	 */
	private AtomicInteger mark = new AtomicInteger(0);
	
	private final PeerSession peerSession;
//	private final TaskSession taskSession;
	private final TorrentSession torrentSession;
//	private final PeerClientGroup peerClientGroup;
	private final TorrentStreamGroup torrentStreamGroup;
	
	private PeerClient(PeerSession peerSession, TorrentSession torrentSession) {
		super("Peer Client", 4, new PeerMessageHandler(peerSession, torrentSession));
		this.peerSession = peerSession;
//		this.taskSession = torrentSession.taskSession();
		this.torrentSession = torrentSession;
//		this.peerClientGroup = torrentSession.peerClientGroup();
		this.torrentStreamGroup = torrentSession.torrentStreamGroup();
	}
	
	public static final PeerClient newInstance(PeerSession peerSession, TorrentSession torrentSession) {
		return new PeerClient(peerSession, torrentSession);
	}

	public PeerSession peerSession() {
		return this.peerSession;
	}

	@Override
	public boolean connect() {
		return connect(this.peerSession.host(), this.peerSession.peerPort());
	}

	/**
	 * 开始下载：发送请求
	 */
	public void launcher() {
		if(this.launcher) {
			return;
		}
		synchronized (this) {
			if(!this.launcher) {
				this.launcher = true;
				this.torrentSession.submit(() -> {
					requests();
				});
			}
		}
	}

	/**
	 * 开始下载：连接、握手
	 */
	public boolean download() {
		LOGGER.debug("Peer连接：{}-{}", this.peerSession.host(), this.peerSession.peerPort());
		final boolean ok = connect();
		if(ok) {
			this.handler.handshake(this);
		} else {
			this.peerSession.fail();
		}
		this.available = ok;
		return ok;
	}
	
	/**
	 * 开始下载：下载种子
	 */
	public boolean torrent() {
		this.handler.torrent();
		return download();
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
			LOGGER.warn("下载的Piece索引不符");
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
		if(available()) {
			LOGGER.debug("PeerClient关闭：{}-{}", this.peerSession.host(), this.peerSession.peerPort());
			this.available = false;
			if(!this.completeLock.get()) { // 没有完成：等待下载完成
				synchronized (this.closeLock) {
					ThreadUtils.wait(this.closeLock, Duration.ofSeconds(CLOSE_AWAIT_TIME));
				}
			}
			this.peerSession.unstatus(PeerConfig.STATUS_DOWNLOAD);
			this.peerSession.peerMessageHandler(null);
			super.close();
		}
	}
	
	/**
	 * 是否可用，Peer优化时如果有不可用的直接剔除
	 */
	public boolean available() {
		return this.available;
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
	 * <p>每次发送{@link #SLICE_MAX_SIZE}个请求，进入等待，当全部数据响应后，又开始发送请求，直到Piece下载完成。</p>
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
			this.completeLock.set(true); // 没有匹配到下载块时设置为完成
			this.release();
			this.torrentSession.completeCheck();
			return false;
		}
		final int index = this.downloadPiece.getIndex();
		while(available()) {
			synchronized (this.countLock) {
				if (this.countLock.get() >= SLICE_MAX_SIZE) {
					ThreadUtils.wait(this.countLock, Duration.ofSeconds(SLICE_AWAIT_TIME));
					if (!this.havePieceMessage) { // 没有数据返回
						break;
					}
				}
				this.countLock.addAndGet(1);
			}
			int begin = this.downloadPiece.position();
			int length = this.downloadPiece.length(); // 顺序不能调换
			this.handler.request(index, begin, length);
			if(!this.downloadPiece.more()) { // 是否还有更多SLICE
				break;
			}
		}
		/*
		 * 此处不论是否有数据返回都需要进行结束等待，防止数据刚刚只有一个slice时直接跳出了slice wait导致响应还没有收到就直接结束了
		 */
		synchronized (this.completeLock) {
			if(!this.completeLock.getAndSet(true)) {
				ThreadUtils.wait(this.completeLock, Duration.ofSeconds(PIECE_AWAIT_TIME));
			}
		}
		if(!this.available) { // 已经关闭唤醒关闭
			synchronized (this.closeLock) {
				this.closeLock.notifyAll();
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
			if(this.downloadPiece.verify()) {
				final boolean ok = this.torrentStreamGroup.piece(this.downloadPiece); // 保存数据
				if(ok) {
					this.peerSession.download(this.downloadPiece.getLength()); // 统计
				}
			} else {
				this.peerSession.badPieces(this.downloadPiece.getIndex());
				LOGGER.warn("下载的Piece校验失败：{}", this.downloadPiece.getIndex());
			}
		} else { // 上次选择的Piece没有完成
			LOGGER.debug("上次选择的Piece未下载完成：{}", this.downloadPiece.getIndex());
		}
		this.downloadPiece = this.torrentStreamGroup.pick(this.peerSession.availablePieces());
		if(LOGGER.isDebugEnabled()) {
			if(this.downloadPiece != null) {
				LOGGER.debug("选取Piece：{}-{}-{}", this.downloadPiece.getIndex(), this.downloadPiece.getBegin(), this.downloadPiece.getEnd());
			}
		}
		synchronized (this.completeLock) {
			this.completeLock.set(false);
		}
		synchronized (this.countLock) {
			this.countLock.set(0);
		}
		this.havePieceMessage = false;
	}
	
	/**
	 * 计算评分：下载速度
	 */
	private void mark(int buffer) {
		this.mark.addAndGet(buffer); // 计算评分
	}

}
