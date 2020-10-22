package com.acgist.snail.net.torrent.peer;

import java.nio.ByteBuffer;

import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.net.TcpMessageHandler;
import com.acgist.snail.net.codec.impl.PeerCryptMessageCodec;
import com.acgist.snail.net.codec.impl.PeerUnpackMessageCodec;
import com.acgist.snail.net.torrent.IMessageEncryptSender;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerSubMessageHandler;

/**
 * <p>Peer消息代理</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class PeerMessageHandler extends TcpMessageHandler implements IMessageEncryptSender {

//	private static final Logger LOGGER = LoggerFactory.getLogger(PeerMessageHandler.class);
	
	/**
	 * <p>Peer消息代理</p>
	 */
	private final PeerSubMessageHandler peerSubMessageHandler;
	/**
	 * <p>加密解密消息代理</p>
	 */
	private final PeerCryptMessageCodec peerCryptMessageCodec;

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
		this.peerSubMessageHandler = peerSubMessageHandler;
		final var peerUnpackMessageCodec = new PeerUnpackMessageCodec(this.peerSubMessageHandler);
		final var peerCryptMessageCodec = new PeerCryptMessageCodec(peerUnpackMessageCodec, this.peerSubMessageHandler);
		this.messageCodec = peerCryptMessageCodec;
		this.peerSubMessageHandler.messageEncryptSender(this);
		this.peerCryptMessageCodec = peerCryptMessageCodec;
	}
	
	@Override
	public void sendEncrypt(ByteBuffer buffer, int timeout) throws NetException {
		this.peerCryptMessageCodec.encode(buffer);
		this.send(buffer, timeout);
	}

}
