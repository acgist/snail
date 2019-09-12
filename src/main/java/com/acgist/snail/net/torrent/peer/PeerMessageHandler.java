package com.acgist.snail.net.torrent.peer;

import java.nio.ByteBuffer;

import com.acgist.snail.net.IMessageEncryptHandler;
import com.acgist.snail.net.TcpMessageHandler;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerCryptMessageHandler;
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
	
	private final PeerSubMessageHandler peerSubMessageHandler;
	private final PeerCryptMessageHandler peerCryptMessageHandler;

	/**
	 * 服务端
	 */
	public PeerMessageHandler() {
		this.peerSubMessageHandler = PeerSubMessageHandler.newInstance();
		this.peerCryptMessageHandler = PeerCryptMessageHandler.newInstance(this.peerSubMessageHandler);
		this.peerSubMessageHandler.messageEncryptHandler(this);
	}

	/**
	 * 客户端
	 */
	public PeerMessageHandler(PeerSubMessageHandler peerSubMessageHandler) {
		this.peerSubMessageHandler = peerSubMessageHandler;
		this.peerCryptMessageHandler = PeerCryptMessageHandler.newInstance(this.peerSubMessageHandler);
		this.peerSubMessageHandler.messageEncryptHandler(this);
	}
	
	@Override
	public void onMessage(ByteBuffer buffer) throws NetException {
		buffer.flip();
		this.peerCryptMessageHandler.onMessage(buffer);
	}
	
	@Override
	public void sendEncrypt(ByteBuffer buffer) throws NetException {
		this.peerCryptMessageHandler.encrypt(buffer);
		this.send(buffer);
	}
	
}
