package com.acgist.snail.net.torrent.peer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.net.codec.MessageCodec;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerSubMessageHandler;
import com.acgist.snail.net.torrent.peer.bootstrap.crypt.MSECryptHandshakeHandler;
import com.acgist.snail.system.config.CryptConfig;
import com.acgist.snail.system.exception.NetException;

/**
 * <p>Peer消息处理：加密、解密</p>
 * 
 * @author acgist
 * @since 1.1.1
 */
public class PeerCryptMessageCodec extends MessageCodec<ByteBuffer, ByteBuffer> {

	/**
	 * MSE加密处理器
	 */
	private final MSECryptHandshakeHandler mseCryptHandshakeHandler;
	
	public PeerCryptMessageCodec(PeerSubMessageHandler peerSubMessageHandler, PeerUnpackMessageCodec peerUnpackMessageCodec) {
		super(peerUnpackMessageCodec);
		this.mseCryptHandshakeHandler = MSECryptHandshakeHandler.newInstance(peerSubMessageHandler, peerUnpackMessageCodec);
	}
	
	@Override
	public void decode(ByteBuffer buffer, InetSocketAddress address, boolean hasAddress) throws NetException {
		buffer.flip();
		if(this.mseCryptHandshakeHandler.over()) { // 握手完成
			this.mseCryptHandshakeHandler.decrypt(buffer);
			this.doNext(buffer, address, hasAddress);
		} else { // 握手
			this.mseCryptHandshakeHandler.handshake(buffer);
		}
	}

	@Override
	public void encode(ByteBuffer buffer) {
		if(this.mseCryptHandshakeHandler.over()) { // 握手完成
			this.mseCryptHandshakeHandler.encrypt(buffer); // 加密消息
		} else {
			if(CryptConfig.STRATEGY.crypt()) { // 加密
				this.mseCryptHandshakeHandler.handshake(); // 握手
				this.mseCryptHandshakeHandler.handshakeLock(); // 加锁
				this.mseCryptHandshakeHandler.encrypt(buffer); // 加密消息
			} else { // 明文
				this.mseCryptHandshakeHandler.plaintext();
			}
		}
	}
	
}
