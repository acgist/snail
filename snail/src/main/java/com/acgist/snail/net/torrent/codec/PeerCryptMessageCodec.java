package com.acgist.snail.net.torrent.codec;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.config.CryptConfig;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.NetException;
import com.acgist.snail.net.codec.MessageCodec;
import com.acgist.snail.net.torrent.peer.PeerSubMessageHandler;

/**
 * <p>Peer消息处理器：加密、解密</p>
 * 
 * @author acgist
 */
public final class PeerCryptMessageCodec extends MessageCodec<ByteBuffer, ByteBuffer> {

	private static final Logger LOGGER = LoggerFactory.getLogger(PeerCryptMessageCodec.class);
	
	/**
	 * <p>MSE加密握手代理</p>
	 */
	private final MSECryptHandshakeHandler mseCryptHandshakeHandler;
	
	/**
	 * @param peerUnpackMessageCodec Peer消息处理器
	 * @param peerSubMessageHandler Peer消息代理
	 */
	public PeerCryptMessageCodec(PeerUnpackMessageCodec peerUnpackMessageCodec, PeerSubMessageHandler peerSubMessageHandler) {
		super(peerUnpackMessageCodec);
		this.mseCryptHandshakeHandler = MSECryptHandshakeHandler.newInstance(peerUnpackMessageCodec, peerSubMessageHandler);
	}
	
	@Override
	public ByteBuffer encode(ByteBuffer buffer) {
		if(this.mseCryptHandshakeHandler.completed()) {
			this.mseCryptHandshakeHandler.encrypt(buffer);
		} else {
			final boolean encrypt = this.mseCryptHandshakeHandler.needEncrypt() && CryptConfig.STRATEGY.crypt();
			if(encrypt) {
				// 需要加密
				this.mseCryptHandshakeHandler.handshake();
				this.mseCryptHandshakeHandler.lockHandshake();
				this.mseCryptHandshakeHandler.encrypt(buffer);
			} else {
				// 不用加密：使用明文完成握手
				this.mseCryptHandshakeHandler.plaintext();
			}
		}
		return buffer;
	}
	
	@Override
	public void doDecode(ByteBuffer buffer, InetSocketAddress address) throws NetException {
		if(this.mseCryptHandshakeHandler.available()) {
			if(this.mseCryptHandshakeHandler.completed()) {
				this.mseCryptHandshakeHandler.decrypt(buffer);
				this.doNext(buffer, address);
			} else {
				this.mseCryptHandshakeHandler.handshake(buffer);
			}
		} else {
			LOGGER.debug("Peer消息代理不可用：忽略消息解密");
		}
	}
	
}
