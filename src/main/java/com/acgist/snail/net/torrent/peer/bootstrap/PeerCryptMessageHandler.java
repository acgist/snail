package com.acgist.snail.net.torrent.peer.bootstrap;

import java.nio.ByteBuffer;

import com.acgist.snail.net.torrent.peer.bootstrap.crypt.MSECryptHandshakeHandler;
import com.acgist.snail.system.config.CryptConfig;
import com.acgist.snail.system.exception.NetException;

/**
 * <p>Peer消息处理：加密、解密</p>
 * 
 * @author acgist
 * @since 1.1.1
 */
public class PeerCryptMessageHandler {

	/**
	 * MSE加密处理器
	 */
	private final MSECryptHandshakeHandler mseCryptHandshakeHandler;
	
	private final PeerUnpackMessageHandler peerUnpackMessageHandler;
	
	private PeerCryptMessageHandler(PeerSubMessageHandler peerSubMessageHandler) {
		this.peerUnpackMessageHandler = PeerUnpackMessageHandler.newInstance(peerSubMessageHandler);
		this.mseCryptHandshakeHandler = MSECryptHandshakeHandler.newInstance(peerSubMessageHandler, this.peerUnpackMessageHandler);
	}
	
	public static final PeerCryptMessageHandler newInstance(PeerSubMessageHandler peerSubMessageHandler) {
		return new PeerCryptMessageHandler(peerSubMessageHandler);
	}
	
	/**
	 * 处理消息
	 * 
	 * @param buffer 读取状态buffer
	 */
	public void onMessage(ByteBuffer buffer) throws NetException {
		if(this.mseCryptHandshakeHandler.over()) { // 握手完成
			this.mseCryptHandshakeHandler.decrypt(buffer);
			this.peerUnpackMessageHandler.onMessage(buffer);
		} else { // 握手
			this.mseCryptHandshakeHandler.handshake(buffer);
		}
	}
	
	/**
	 * 消息加密
	 */
	public void encrypt(ByteBuffer buffer) {
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
