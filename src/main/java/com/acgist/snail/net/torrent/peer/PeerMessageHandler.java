package com.acgist.snail.net.torrent.peer;

import java.nio.ByteBuffer;

import com.acgist.snail.net.TcpMessageHandler;
import com.acgist.snail.net.torrent.IMessageEncryptHandler;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerSubMessageHandler;
import com.acgist.snail.system.exception.NetException;

/**
 * <p>Peer消息</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class PeerMessageHandler extends TcpMessageHandler implements IMessageEncryptHandler {

//	private static final Logger LOGGER = LoggerFactory.getLogger(PeerMessageHandler.class);
	
	/**
	 * Peer代理
	 */
	private final PeerSubMessageHandler peerSubMessageHandler;

	/**
	 * 服务端
	 */
	public PeerMessageHandler() {
		this.peerSubMessageHandler = PeerSubMessageHandler.newInstance();
		final var peerUnpackMessageCodec = new PeerUnpackMessageCodec(this.peerSubMessageHandler);
		final var peerCryptMessageCodec = new PeerCryptMessageCodec(this.peerSubMessageHandler, peerUnpackMessageCodec);
		this.messageCodec = peerCryptMessageCodec;
		this.peerSubMessageHandler.messageEncryptHandler(this);
	}

	/**
	 * 客户端
	 */
	public PeerMessageHandler(PeerSubMessageHandler peerSubMessageHandler) {
		this.peerSubMessageHandler = peerSubMessageHandler;
		final var peerUnpackMessageCodec = new PeerUnpackMessageCodec(this.peerSubMessageHandler);
		final var peerCryptMessageCodec = new PeerCryptMessageCodec(this.peerSubMessageHandler, peerUnpackMessageCodec);
		this.messageCodec = peerCryptMessageCodec;
		this.peerSubMessageHandler.messageEncryptHandler(this);
	}
	
	@Override
	public void sendEncrypt(ByteBuffer buffer) throws NetException {
		this.messageCodec.encode(buffer);
		this.send(buffer);
	}

}
