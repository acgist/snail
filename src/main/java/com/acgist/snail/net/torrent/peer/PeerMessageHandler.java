package com.acgist.snail.net.torrent.peer;

import java.nio.ByteBuffer;

import com.acgist.snail.net.TcpMessageHandler;
import com.acgist.snail.net.torrent.PeerUnpackMessageHandler;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerSubMessageHandler;
import com.acgist.snail.system.exception.NetException;

/**
 * <p>Peer消息处理（TCP）</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class PeerMessageHandler extends TcpMessageHandler {

//	private static final Logger LOGGER = LoggerFactory.getLogger(PeerMessageHandler.class);
	
	private final PeerSubMessageHandler peerSubMessageHandler;
	private final PeerUnpackMessageHandler peerUnpackMessageHandler;

	/**
	 * 服务端
	 */
	public PeerMessageHandler() {
		this.peerSubMessageHandler = PeerSubMessageHandler.newInstance();
		this.peerUnpackMessageHandler = PeerUnpackMessageHandler.newInstance(this.peerSubMessageHandler);
		this.peerSubMessageHandler.messageHandler(this);
	}

	/**
	 * 客户端
	 */
	public PeerMessageHandler(PeerSubMessageHandler peerSubMessageHandler) {
		this.peerSubMessageHandler = peerSubMessageHandler;
		this.peerUnpackMessageHandler = PeerUnpackMessageHandler.newInstance(this.peerSubMessageHandler);
		this.peerSubMessageHandler.messageHandler(this);
	}
	
	@Override
	public void onMessage(ByteBuffer attachment) throws NetException {
		attachment.flip();
		this.peerUnpackMessageHandler.onMessage(attachment);
	}
	
}
