package com.acgist.snail.net.peer;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.torrent.bootstrap.TorrentStreamGroup;
import com.acgist.snail.net.TcpClient;
import com.acgist.snail.pojo.TorrentPiece;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.utils.ThreadUtils;

/**
 * Peer客户端<br>
 * 基本协议：TCP<br>
 */
public class PeerClient extends TcpClient<PeerMessageHandler> {
	
	private static final int SLICE_MAX_SIZE = 10; // 单次请求10个SLICE
	
	private static final int SLICE_AWAIT_TIME = 10; // SLICE每批等待时间
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
	private AtomicBoolean overLock = new AtomicBoolean(false); // Piece完成锁
	private AtomicInteger countLock = new AtomicInteger(0); // Piece分片锁
	
	private AtomicInteger mark = new AtomicInteger(0); // 评分：下载速度
	
	private final PeerSession peerSession;
//	private final TaskSession taskSession;
	private final TorrentSession torrentSession;
//	private final PeerClientGroup peerClientGroup;
	private final TorrentStreamGroup torrentStreamGroup;
	
	public PeerClient(PeerSession peerSession, TorrentSession torrentSession) {
		super("Peer", 4, new PeerMessageHandler(peerSession, torrentSession));
		this.peerSession = peerSession;
//		this.taskSession = torrentSession.taskSession();
		this.torrentSession = torrentSession;
//		this.peerClientGroup = torrentSession.peerClientGroup();
		this.torrentStreamGroup = torrentSession.torrentStreamGroup();
	}

	public PeerSession peerSession() {
		return this.peerSession;
	}

	@Override
	public boolean connect() {
		return connect(peerSession.host(), peerSession.port());
	}

	/**
	 * 开始
	 */
	public void launcher() {
		if(launcher) {
			return;
		}
		synchronized (this) {
			if(!launcher) {
				launcher = true;
				torrentSession.submit(() -> {
					request();
				});
			}
		}
	}

	/**
	 * 开始下载
	 */
	public boolean download() {
		LOGGER.debug("Peer连接：{}:{}", peerSession.host(), peerSession.port());
		final boolean ok = connect();
		if(ok) {
			handler.handshake(this);
		}
		this.available = ok;
		return ok;
	}
	
	/**
	 * 下载种子
	 */
	public boolean torrent() {
		handler.torrent();
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
		if(index != downloadPiece.getIndex()) {
			LOGGER.warn("下载的Piece索引不符");
			return;
		}
		this.havePieceMessage = true;
		mark(bytes.length); // 每次获取到都需要打分
		synchronized (countLock) { // 唤醒request
			if (countLock.addAndGet(-1) <= 0) {
				countLock.notifyAll();
			}
		}
		final boolean over = downloadPiece.put(begin, bytes); // 下载完成
		if(over) { // 唤醒下载完成
			synchronized (overLock) {
				if (overLock.getAndSet(true)) {
					overLock.notifyAll();
				}
			}
		}
	}
	
	/**
	 * 资源释放
	 */
	public void release() {
		if(available()) {
			this.available = false;
			if(!overLock.get()) { // 没有完成：等待下载完成
				synchronized (closeLock) {
					ThreadUtils.wait(closeLock, Duration.ofSeconds(CLOSE_AWAIT_TIME));
				}
			}
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
	 * 发送have消息
	 */
	public void have(int index) {
		handler.have(index);
	}
	
	/**
	 * 获取评分<br>
	 * 每次获取后清空
	 */
	public int mark() {
		return mark.getAndSet(0);
	}

	/**
	 * 下载失败
	 */
	private void undone() {
		LOGGER.debug("下载失败：{}", this.downloadPiece.getIndex());
		torrentStreamGroup.undone(this.downloadPiece);
	}

	/**
	 * 请求数据
	 */
	private void request() {
		if(!available()) {
			return;
		}
		pickDownloadPiece();
		if(this.downloadPiece == null) {
			LOGGER.debug("没有匹配Peer块下载");
			this.release();
			torrentSession.over();
			return;
		}
		final int index = this.downloadPiece.getIndex();
		while(available()) {
			synchronized (countLock) {
				if (countLock.get() >= SLICE_MAX_SIZE) {
					ThreadUtils.wait(countLock, Duration.ofSeconds(SLICE_AWAIT_TIME));
					if (!this.havePieceMessage) { // 没有数据返回
						break;
					}
				}
				countLock.addAndGet(1);
			}
			int begin = this.downloadPiece.position();
			int length = this.downloadPiece.length(); // 顺序不能调换
			handler.request(index, begin, length);
			if(!downloadPiece.more()) { // 是否还有更多SLICE
				break;
			}
		}
		/*
		 * 此处不论是否有数据返回都需要进行结束等待，防止数据刚刚只有一个slice时直接跳出了slice wait导致响应还没有收到就直接结束了
		 */
		synchronized (overLock) {
			if(!overLock.getAndSet(true)) {
				ThreadUtils.wait(overLock, Duration.ofSeconds(PIECE_AWAIT_TIME));
			}
		}
		if(!available) { // 已经关闭
			synchronized (closeLock) {
				closeLock.notifyAll();
			}
		}
		if(countLock.get() > 0) { // 没有下载完成
			undone();
		}
		request();
	}

	/**
	 * 选择下载Piece
	 */
	private void pickDownloadPiece() {
		if(!available()) { // 不可用
			return;
		}
		if(this.downloadPiece != null && this.downloadPiece.over()) {
			peerSession.statistics(downloadPiece.getLength()); // 统计
			torrentStreamGroup.piece(downloadPiece); // 保存数据
		}
		this.downloadPiece = torrentStreamGroup.pick(peerSession.pieces());
		if(LOGGER.isDebugEnabled()) {
			if(downloadPiece != null) {
				LOGGER.debug("选取Piece：{}-{}-{}", downloadPiece.getIndex(), downloadPiece.getBegin(), downloadPiece.getEnd());
			}
		}
		synchronized (overLock) {
			overLock.set(false);
		}
		synchronized (countLock) {
			countLock.set(0);
		}
		this.havePieceMessage = false;
	}
	
	/**
	 * 计算评分：下载速度
	 */
	private void mark(int buffer) {
		mark.addAndGet(buffer); // 计算评分
	}

}
