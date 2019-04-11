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
	
	private TorrentPiece downloadPiece; // 下载的Piece信息
	
	private AtomicInteger count = new AtomicInteger(0);
	
	private final PeerSession peerSession;
//	private final TaskSession taskSession;
//	private final TorrentSession torrentSession;
	private final TorrentStreamGroup torrentStreamGroup;
	
	public PeerClient(PeerSession peerSession, TorrentSession torrentSession) {
		super("Peer", 10, new PeerMessageHandler(peerSession, torrentSession));
		this.peerSession = peerSession;
//		this.taskSession = torrentSession.taskSession();
//		this.torrentSession = torrentSession;
		this.torrentStreamGroup = torrentSession.torrentStreamGroup();
	}

	@Override
	public boolean connect() {
		boolean ok = connect(peerSession.host(), peerSession.port());
		if(ok) {
			handler.handshake(this);
		}
		return ok;
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
		boolean over = downloadPiece.put(begin, bytes);
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
			LOGGER.debug("没有找到Peer块下载");
			// TODO：close()：group.优化
			return;
		}
		final int index = this.downloadPiece.getIndex();
		while(true) {
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
	
	public void release() {
	}
	
	/**
	 * 选择下载Piece
	 */
	private void repick() {
		if(this.downloadPiece != null && this.downloadPiece.over()) {
			peerSession.statistics(downloadPiece.getLength()); // 统计
			torrentStreamGroup.piece(downloadPiece); // 保存数据
		}
		this.downloadPiece = torrentStreamGroup.pick(peerSession.bitSet());
		request();
		count.set(0);
	}

}
