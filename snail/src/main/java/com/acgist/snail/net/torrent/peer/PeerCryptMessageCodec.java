package com.acgist.snail.net.torrent.peer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.CryptConfig;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.net.codec.MessageCodec;
import com.acgist.snail.net.torrent.crypt.MSECryptHandshakeHandler;

/**
 * <p>Peer消息处理器：加密、解密</p>
 * 
 * @author acgist
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
	 * @param peerUnpackMessageCodec Peer消息代理
	 * @param peerSubMessageHandler MSE加密握手代理
	 */
	public PeerCryptMessageCodec(PeerUnpackMessageCodec peerUnpackMessageCodec, PeerSubMessageHandler peerSubMessageHandler) {
		super(peerUnpackMessageCodec);
		this.peerSubMessageHandler = peerSubMessageHandler;
		this.mseCryptHandshakeHandler = MSECryptHandshakeHandler.newInstance(peerUnpackMessageCodec, peerSubMessageHandler);
	}
	
	@Override
	public ByteBuffer encode(ByteBuffer buffer) {
//		buffer = super.encode(buffer); // 不用编码：提高性能
		if(this.mseCryptHandshakeHandler.complete()) { // 握手完成
			this.mseCryptHandshakeHandler.encrypt(buffer); // 加密消息
		} else { // 握手未完成
			// 通过Peer加密策略结合软件加密策略决定是否加密
			final boolean encrypt = this.mseCryptHandshakeHandler.needEncrypt() && CryptConfig.STRATEGY.crypt();
			if(encrypt) { // 需要加密
				this.mseCryptHandshakeHandler.handshake(); // 握手
				this.mseCryptHandshakeHandler.lockHandshake(); // 握手加锁
				this.mseCryptHandshakeHandler.encrypt(buffer); // 加密消息
			} else { // 不需要加密：使用明文完成握手
				this.mseCryptHandshakeHandler.plaintext();
			}
		}
		return buffer;
	}
	
	@Override
	public void doDecode(ByteBuffer buffer, InetSocketAddress address) throws NetException {
		if(this.peerSubMessageHandler.available()) { // 可用
			buffer.flip(); // 后续消息处理器不需要调用此方法
			if(this.mseCryptHandshakeHandler.complete()) { // 握手完成
				this.mseCryptHandshakeHandler.decrypt(buffer);
				this.doNext(buffer, address);
			} else { // 握手消息
				this.mseCryptHandshakeHandler.handshake(buffer);
			}
		} else { // 不可用
			LOGGER.debug("Peer消息代理不可用：忽略消息解密");
		}
	}
	
}
