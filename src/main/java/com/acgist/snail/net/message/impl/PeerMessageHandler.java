package com.acgist.snail.net.message.impl;

import java.nio.ByteBuffer;

import com.acgist.snail.net.message.TcpMessageHandler;
import com.acgist.snail.net.peer.PeerServer;
import com.acgist.snail.pojo.session.TorrentSession;

/**
 * Peer消息处理
 */
public class PeerMessageHandler extends TcpMessageHandler {

	/**
	 * 握手协议名称
	 */
	public static final String HANDSHAKE_NAME = "BitTorrent protocol";
	
	private TorrentSession torrentSession;
	
	public PeerMessageHandler(TorrentSession torrentSession) {
		super("");
		this.torrentSession = torrentSession;
	}

	@Override
	public boolean doMessage(Integer result, ByteBuffer attachment) {
		return true;
	}

	/**
	 * 握手：
	 * 消息格式：<pstrlen><pstr><reserved><info_hash><peer_id>
	 * pstrlen：pstr的长度：19<br>
	 * pstr：BitTorrent协议的关键字：BitTorrent protocol<br>
	 * reserved：8字节，用于扩展BT协议，一般都设置：0<br>
	 * info_hash：info_hash<br>
	 * peer_id：peer_id
	 */
	public void handshake() {
		ByteBuffer buffer = ByteBuffer.allocate(71);
		buffer.putInt(19);
		buffer.put(HANDSHAKE_NAME.getBytes());
		buffer.put("00000000".getBytes());
		buffer.put(torrentSession.infoHash().hash());
		buffer.put(PeerServer.PEER_ID.getBytes());
		send(buffer.array());
	}

	/**
	 * 消息持久：<len=0000>
	 * 只有消息长度，没有消息编号和负载
	 */
	public void keepAlive() {
	}
	
	/**
	 * 5字节：<len=0001><id=0>
	 */
	public void choke() {
	}
	
	/**
	 * 5字节：<len=0001><id=1>
	 */
	public void unchoke() {
	}
	
	/**
	 * 5字节：<len=0001><id=2>
	 */
	public void interested() {
	}
	
	/**
	 * 5字节：<len=0001><id=3>
	 */
	public void notInterested() {
	}
	
	/**
	 * 5字节：<len=0005><id=4><piece index>
	 */
	public void have() {
	}
	
	/**
	 * 消息：
	 * 消息格式：&lt;length prefix&gt;&lt;message ID&gt;&lt;payload&gt;<br>
	 * length prefix：4字节：message id和payload的长度和<br>
	 * message id：1字节：指明消息的编号<br>
	 * payload：消息内容
	 */
	public ByteBuffer buildMessage(Integer length, Byte id, byte[] payload) {
		int capacity = 4;
		if(id != null) {
			capacity += 1;
		}
		if(payload != null) {
			capacity += payload.length;
		}
		ByteBuffer buffer = ByteBuffer.allocate(capacity);
		buffer.putInt(length);
		if(id != null) {
			buffer.put(id);
		}
		if(payload != null) {
			buffer.put(payload);
		}
		return buffer;
	}

}
