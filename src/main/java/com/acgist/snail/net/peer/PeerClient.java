package com.acgist.snail.net.peer;

import com.acgist.snail.net.TcpClient;
import com.acgist.snail.net.message.impl.PeerMessageHandler;
import com.acgist.snail.pojo.session.PeerSession;

/**
 * Peer客户端<br>
 * 基本协议：TCP<br>
 * 位操作：BitSet<br>
 * https://blog.csdn.net/p312011150/article/details/81478237
 */
public class PeerClient extends TcpClient<PeerMessageHandler> {

	private PeerSession peerSession;
	
	public PeerClient() {
		super("", new PeerMessageHandler());
	}

	@Override
	public boolean connect() {
		return false;
	}

}
