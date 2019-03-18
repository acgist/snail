package com.acgist.snail.net.peer;

import com.acgist.snail.net.AbstractTcpClient;
import com.acgist.snail.net.message.impl.PeerMessageHandler;

/**
 * Peer客户端<br>
 * 基本协议：TCP<br>
 * https://blog.csdn.net/p312011150/article/details/81478237
 */
public class PeerClient extends AbstractTcpClient<PeerMessageHandler> {

	public PeerClient() {
		super("", new PeerMessageHandler());
	}

	@Override
	public boolean connect() {
		return false;
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
	private void handshake() {
	}

	/**
	 * 发送消息：
	 * 消息格式：<length prefix><message ID><payload>
	 * length prefix：4字节：message id和payload的长度和<br>
	 * message id：1字节：指明消息的编号<br>
	 * payload：消息内容
	 */
	public void message() {
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
	
}
