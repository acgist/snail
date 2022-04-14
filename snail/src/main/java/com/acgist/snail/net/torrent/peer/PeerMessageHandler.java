package com.acgist.snail.net.torrent.peer;

import java.nio.ByteBuffer;

import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.net.TcpMessageHandler;
import com.acgist.snail.net.codec.IMessageEncoder;
import com.acgist.snail.net.torrent.IEncryptMessageSender;
import com.acgist.snail.net.torrent.IPeerConnect;
import com.acgist.snail.net.torrent.codec.PeerCryptMessageCodec;
import com.acgist.snail.net.torrent.codec.PeerUnpackMessageCodec;

/**
 * <p>Peer消息代理</p>
 * 
 * @author acgist
 */
public final class PeerMessageHandler extends TcpMessageHandler implements IEncryptMessageSender {

	/**
	 * <p>消息编码器</p>
	 */
	private final IMessageEncoder<ByteBuffer> messageEncoder;
	/**
	 * <p>Peer消息代理</p>
	 */
	private final PeerSubMessageHandler peerSubMessageHandler;
	
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
		this.messageDecoder = peerCryptMessageCodec;
		this.messageEncoder = peerCryptMessageCodec;
		this.peerSubMessageHandler = peerSubMessageHandler;
	}
	
	@Override
	public boolean useless() {
		return this.peerSubMessageHandler.useless();
	}
	
	@Override
	public void sendEncrypt(ByteBuffer buffer, int timeout) throws NetException {
		this.messageEncoder.encode(buffer);
		this.send(buffer, timeout);
	}
	
	@Override
	public IPeerConnect.ConnectType connectType() {
		return IPeerConnect.ConnectType.TCP;
	}

}
