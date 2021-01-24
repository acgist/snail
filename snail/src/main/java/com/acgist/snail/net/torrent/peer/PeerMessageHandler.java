package com.acgist.snail.net.torrent.peer;

import java.nio.ByteBuffer;

import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.net.IMessageEncryptSender;
import com.acgist.snail.net.TcpMessageHandler;

/**
 * <p>Peer消息代理</p>
 * 
 * @author acgist
 */
public final class PeerMessageHandler extends TcpMessageHandler implements IMessageEncryptSender {

	/**
	 * <p>服务端</p>
	 */
	public PeerMessageHandler() {
		this(PeerSubMessageHandler.newInstance());
	}

	/**
	 * <p>客户端</p>
	 * 
	 * @param peerSubMessageHandler Peer消息代理
	 */
	public PeerMessageHandler(PeerSubMessageHandler peerSubMessageHandler) {
		peerSubMessageHandler.messageEncryptSender(this);
		final var peerUnpackMessageCodec = new PeerUnpackMessageCodec(peerSubMessageHandler);
		final var peerCryptMessageCodec = new PeerCryptMessageCodec(peerUnpackMessageCodec, peerSubMessageHandler);
		this.messageCodec = peerCryptMessageCodec;
	}
	
	@Override
	public void sendEncrypt(ByteBuffer buffer, int timeout) throws NetException {
		this.messageCodec.encode(buffer);
		this.send(buffer, timeout);
	}

}
