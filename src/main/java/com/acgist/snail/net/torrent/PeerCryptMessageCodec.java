package com.acgist.snail.net.torrent;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.net.codec.MessageCodec;
import com.acgist.snail.net.torrent.crypt.MSECryptHandshakeHandler;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerSubMessageHandler;
import com.acgist.snail.system.config.CryptConfig;
import com.acgist.snail.system.exception.NetException;

/**
 * <p>Peer消息处理器：加密、解密</p>
 * 
 * @author acgist
 * @since 1.1.1
 */
public final class PeerCryptMessageCodec extends MessageCodec<ByteBuffer, ByteBuffer> {

	/**
	 * MSE加密握手代理
	 */
	private final MSECryptHandshakeHandler mseCryptHandshakeHandler;
	
	public PeerCryptMessageCodec(PeerUnpackMessageCodec peerUnpackMessageCodec, PeerSubMessageHandler peerSubMessageHandler) {
		super(peerUnpackMessageCodec);
		this.mseCryptHandshakeHandler = MSECryptHandshakeHandler.newInstance(peerUnpackMessageCodec, peerSubMessageHandler);
	}
	
	@Override
	public void decode(ByteBuffer buffer, InetSocketAddress address, boolean hasAddress) throws NetException {
		buffer.flip();
		if(this.mseCryptHandshakeHandler.complete()) { // 握手完成
			this.mseCryptHandshakeHandler.decrypt(buffer);
			this.doNext(buffer, address, hasAddress);
		} else { // 握手消息
			this.mseCryptHandshakeHandler.handshake(buffer);
		}
	}

	@Override
	public void encode(ByteBuffer buffer) {
		this.messageCodec.encode(buffer);
		if(this.mseCryptHandshakeHandler.complete()) { // 握手完成
			this.mseCryptHandshakeHandler.encrypt(buffer); // 加密消息
		} else { // 握手未完成
			/*
			 * 优先Peer是否偏爱加密，再判断软件本身配置加密策略。
			 */
			final boolean needEncrypt = this.mseCryptHandshakeHandler.needEncrypt();
			final boolean encrypt = needEncrypt ? true : CryptConfig.STRATEGY.crypt();
			if(encrypt) { // 需要加密
				this.mseCryptHandshakeHandler.handshake(); // 握手
				this.mseCryptHandshakeHandler.handshakeLock(); // 握手加锁
				this.mseCryptHandshakeHandler.encrypt(buffer); // 加密消息
			} else { // 不需要加密：使用明文完成握手
				this.mseCryptHandshakeHandler.plaintext();
			}
		}
	}
	
}
