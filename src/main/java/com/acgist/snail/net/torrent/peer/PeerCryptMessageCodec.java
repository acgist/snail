package com.acgist.snail.net.torrent.peer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.net.codec.MessageCodec;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerSubMessageHandler;
import com.acgist.snail.net.torrent.peer.bootstrap.crypt.MSECryptHandshakeHandler;
import com.acgist.snail.system.config.CryptConfig;
import com.acgist.snail.system.exception.NetException;

/**
 * <p>Peer消息处理器：加密、解密</p>
 * 
 * @author acgist
 * @since 1.1.1
 */
public class PeerCryptMessageCodec extends MessageCodec<ByteBuffer, ByteBuffer> {

	/**
	 * MSE加密握手代理
	 */
	private final MSECryptHandshakeHandler mseCryptHandshakeHandler;
	
	public PeerCryptMessageCodec(PeerSubMessageHandler peerSubMessageHandler, PeerUnpackMessageCodec peerUnpackMessageCodec) {
		super(peerUnpackMessageCodec);
		this.mseCryptHandshakeHandler = MSECryptHandshakeHandler.newInstance(peerSubMessageHandler, peerUnpackMessageCodec);
	}
	
	@Override
	public void decode(ByteBuffer buffer, InetSocketAddress address, boolean hasAddress) throws NetException {
		buffer.flip();
		// 握手完成
		if(this.mseCryptHandshakeHandler.over()) {
			this.mseCryptHandshakeHandler.decrypt(buffer);
			this.doNext(buffer, address, hasAddress);
		} else { // 握手消息
			this.mseCryptHandshakeHandler.handshake(buffer);
		}
	}

	@Override
	public void encode(ByteBuffer buffer) {
		if(this.mseCryptHandshakeHandler.over()) { // 握手完成
			this.mseCryptHandshakeHandler.encrypt(buffer); // 加密消息
		} else { // 握手未完成
			if(CryptConfig.STRATEGY.crypt()) { // 需要加密
				this.mseCryptHandshakeHandler.handshake(); // 握手
				this.mseCryptHandshakeHandler.handshakeLock(); // 握手加锁
				this.mseCryptHandshakeHandler.encrypt(buffer); // 加密消息
			} else { // 不需要加密：使用明文完成握手
				this.mseCryptHandshakeHandler.plaintext();
			}
		}
	}
	
}
