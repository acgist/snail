package com.acgist.snail.net.peer;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.torrent.bootstrap.TorrentStreamGroup;
import com.acgist.snail.net.TcpClient;
import com.acgist.snail.pojo.TorrentPiece;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TorrentSession;

/**
 * Peer客户端<br>
 * 基本协议：TCP<br>
 */
public class PeerClient extends TcpClient<PeerMessageHandler> {
	
	private static final int MAX_SLICE_SIZE = 10; // 单次请求10个SLICE

	private static final Logger LOGGER = LoggerFactory.getLogger(PeerClient.class);
	
	private boolean available = false; // 状态
	private TorrentPiece downloadPiece; // 下载的Piece信息
	
	private AtomicInteger mark = new AtomicInteger(0); // 评分：下载速度
	private AtomicInteger count = new AtomicInteger(0);
	
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

	/**
	 * 开始下载
	 */
	public boolean download() {
		LOGGER.debug("Peer连接：{}:{}", peerSession.host(), peerSession.port());
		final boolean ok = connect();
		if(ok) {
			handler.handshake(this);
			LOGGER.debug("Peer握手：{}:{}", peerSession.host(), peerSession.port());
		}
		this.available = ok;
		return ok;
	}
	
	@Override
	public boolean connect() {
		return connect(peerSession.host(), peerSession.port());
	}
	
	public PeerSession peerSession() {
		return this.peerSession;
	}

	public void piece(int index, int begin, byte[] bytes) {
		LOGGER.debug("响应：index：{}，begin：{}，length：{}", index, begin, (bytes == null ? null : bytes.length));
		if(bytes == null) { // 数据不完整抛弃当前块，重新选择下载块
			peerSession.unBitSet(index);
			torrentStreamGroup.undone(index);
			repick();
			return;
		}
		if(index != downloadPiece.getIndex()) {
			LOGGER.warn("下载的Piece索引不符");
			return;
		}
		final boolean over = downloadPiece.put(begin, bytes);
		if(over) { // 下载完成
			repick();
		}
		if(count.addAndGet(-1) <= 0) {
			request();
		}
	}

	/**
	 * 开始
	 */
	public void launcher() {
		synchronized (this) {
			if(downloadPiece == null) {
				repick();
			}
		}
	}
	
	/**
	 * 请求数据
	 */
	private void request() {
		if(this.downloadPiece == null) {
			LOGGER.debug("没有匹配Peer块下载");
			this.release();
			torrentSession.over();
			return;
		}
		final int index = this.downloadPiece.getIndex();
		while(available) {
			if(count.get() >= MAX_SLICE_SIZE) {
				break;
			}
			count.addAndGet(1);
			int begin = this.downloadPiece.position();
			int length = this.downloadPiece.length(); // 顺序不能调换
			if(length == 0) { // 已经下载完成
				break;
			}
			LOGGER.debug("请求：index：{}，begin：{}，length：{}", index, begin, length);
			handler.request(index, begin, length);
		}
	}

	@Override
	public void close() {
		super.close();
	}
	
	/**
	 * 资源释放
	 */
	public void release() {
		if(available) {
			available = false;
			close();
		}
	}
	
	/**
	 * 是否可用，Peer优化时如果有不可用的直接剔除
	 */
	public boolean available() {
		return available;
	}
	
	/**
	 * 发送have消息
	 */
	public void have(int index) {
		handler.have(index);
	}
	
	/**
	 * 选择下载Piece
	 */
	private void repick() {
		if(!available) { // 不可用
			return;
		}
		if(this.downloadPiece != null && this.downloadPiece.over()) {
			mark(downloadPiece.getLength());
			peerSession.statistics(downloadPiece.getLength()); // 统计
			torrentStreamGroup.piece(downloadPiece); // 保存数据
		}
		this.downloadPiece = torrentStreamGroup.pick(peerSession.bitSet());
		request();
		count.set(0);
	}
	
	/**
	 * 获取评分<br>
	 * 每次获取后清空
	 */
	public int mark() {
		return mark.getAndSet(0);
	}

	/**
	 * 计算评分：下载速度
	 */
	private void mark(int buffer) {
		mark.addAndGet(buffer); // 计算评分
	}

}
