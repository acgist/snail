package com.acgist.snail.net.codec.impl;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.CryptConfig;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.net.codec.MessageCodec;
import com.acgist.snail.net.torrent.crypt.MSECryptHandshakeHandler;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerSubMessageHandler;

/**
 * <p>Peer消息处理器：加密、解密</p>
 * 
 * @author acgist
 * @since 1.1.1
 */
public final class PeerCryptMessageCodec extends MessageCodec<ByteBuffer, ByteBuffer> {

	private static final Logger LOGGER = LoggerFactory.getLogger(PeerCryptMessageCodec.class);
	
	/**
	 * <p>Peer消息代理</p>
	 */
	private final PeerSubMessageHandler peerSubMessageHandler;
	/**
	 * <p>MSE加密握手代理</p>
	 */
	private final MSECryptHandshakeHandler mseCryptHandshakeHandler;
	
	/**
	 * <p>Peer消息处理器</p>
	 * 
	 * @param peerUnpackMessageCodec Peer消息代理
	 * @param peerSubMessageHandler MSE加密握手代理
	 */
	public PeerCryptMessageCodec(PeerUnpackMessageCodec peerUnpackMessageCodec, PeerSubMessageHandler peerSubMessageHandler) {
		super(peerUnpackMessageCodec);
		this.peerSubMessageHandler = peerSubMessageHandler;
		this.mseCryptHandshakeHandler = MSECryptHandshakeHandler.newInstance(peerUnpackMessageCodec, peerSubMessageHandler);
	}
	
	@Override
	public void decode(ByteBuffer buffer, InetSocketAddress address, boolean haveAddress) throws NetException {
		if(this.peerSubMessageHandler.available()) { // 可用
			buffer.flip(); // 后续消息处理器不需要调用此方法
			if(this.mseCryptHandshakeHandler.complete()) { // 握手完成
				this.mseCryptHandshakeHandler.decrypt(buffer);
				this.doNext(buffer, address, haveAddress);
			} else { // 握手消息
				this.mseCryptHandshakeHandler.handshake(buffer);
			}
		} else { // 不可用
			LOGGER.debug("Peer消息代理不可用：忽略消息解密");
		}
	}

	@Override
	public void encode(ByteBuffer buffer) {
		this.messageCodec.encode(buffer);
		if(this.mseCryptHandshakeHandler.complete()) { // 握手完成
			this.mseCryptHandshakeHandler.encrypt(buffer); // 加密消息
		} else { // 握手未完成
			/*
			 * 优先验证Peer是否偏爱加密，再验证软件本身配置加密策略。
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
