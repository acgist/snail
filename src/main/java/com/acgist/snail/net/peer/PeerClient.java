package com.acgist.snail.net.peer;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.torrent.bootstrap.TorrentStreamGroup;
import com.acgist.snail.net.TcpClient;
import com.acgist.snail.pojo.TorrentPiece;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TaskSession;
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
	private final TaskSession taskSession;
	private final TorrentSession torrentSession;
	private final TorrentStreamGroup torrentStreamGroup;
	
	public PeerClient(PeerSession peerSession, TorrentSession torrentSession) {
		super("Peer", 10, new PeerMessageHandler(peerSession, torrentSession));
		this.peerSession = peerSession;
		this.taskSession = torrentSession.taskSession();
		this.torrentSession = torrentSession;
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
		LOGGER.debug("响应：index：{}，begin：{}，length：{}", index, begin, bytes.length);
		if(index != downloadPiece.getIndex()) {
			LOGGER.warn("下载的Piece索引不符");
			return;
		}
		boolean over = downloadPiece.put(begin, bytes);
		if(over) {
			pick();
			request();
			count.set(0);
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
				pick();
				request();
			}
		}
	}
	
	private void request() {
		if(this.downloadPiece == null) {
			LOGGER.debug("没有找到Peer块下载");
			return;
		}
		final int index = this.downloadPiece.getIndex();
		while(true) {
			if(count.get() >= MAX_SLICE_SIZE) {
				break;
			}
			count.addAndGet(1);
			int begin = this.downloadPiece.position();
			int length = this.downloadPiece.length();
			if(length == 0) {
				break;
			}
			LOGGER.debug("请求：begin：{}，length：{}", begin, length);
			handler.request(index, begin, length);
		}
	}
	
	public void release() {
	}
	
	/**
	 * 选择下载Piece
	 */
	private void pick() {
		if(this.downloadPiece != null && this.downloadPiece.over()) {
			torrentStreamGroup.piece(downloadPiece); // 保存数据
		}
		this.downloadPiece = torrentStreamGroup.pick(peerSession.bitSet());
	}

}
