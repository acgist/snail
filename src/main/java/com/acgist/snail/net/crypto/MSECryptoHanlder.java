package com.acgist.snail.net.crypto;

import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.util.Random;

import com.acgist.snail.net.torrent.peer.bootstrap.PeerSubMessageHandler;
import com.acgist.snail.system.config.CryptoConfig;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.ArrayUtils;

/**
 * <p>MSE密钥工具</p>
 * <p>加密算法：ARC4</p>
 * <p>密钥交换算法：DH(Diffie-Hellman)</p>
 * <p>代码参考：https://github.com/atomashpolskiy/bt</p>
 * <p>参考链接：http://wiki.vuze.com/w/Message_Stream_Encryption</p>
 * <p>参考链接：https://wiki.openssl.org/index.php/Diffie_Hellman</p>
 * <p>步骤：</p>
 * <pre>
 * 1 A->B: Diffie Hellman Ya, PadA
 * 2 B->A: Diffie Hellman Yb, PadB
 * 3 A->B:
 * 	HASH('req1', S),
 * 	HASH('req2', SKEY) xor HASH('req3', S),
 * 	ENCRYPT(VC, crypto_provide, len(PadC), PadC, len(IA)),
 * 	ENCRYPT(IA)
 * 4 B->A:
 * 	ENCRYPT(VC, crypto_select, len(padD), padD),
 * 	ENCRYPT2(Payload Stream)
 * 5 A->B: ENCRYPT2(Payload Stream)
 * </pre>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class MSECryptoHanlder {

	/**
	 * 一共步骤：5
	 */
	private static final int MAX_STEP = 5;
	/**
	 * 当前步骤
	 */
	private int step = 0;
	/**
	 * 是否加密：默认明文
	 */
	private volatile boolean crypt = false;
	/**
	 * 是否处理完成（握手处理）
	 */
	private volatile boolean over = false;
	/**
	 * 是否继续处理
	 */
	private boolean next = false;

	private final MSEKeyPairBuilder builder;
	private final PeerSubMessageHandler peerSubMessageHandler;

	private MSECryptoHanlder(PeerSubMessageHandler peerSubMessageHandler) {
		this.builder = MSEKeyPairBuilder.newInstance();
		this.peerSubMessageHandler = peerSubMessageHandler;
	}

	public static final MSECryptoHanlder newInstance(PeerSubMessageHandler peerSubMessageHandler) {
		return new MSECryptoHanlder(peerSubMessageHandler);
	}

	/**
	 * 是否加密
	 */
	public boolean crypt() {
		return this.crypt;
	}

	/**
	 * 是否处理完成
	 */
	public boolean over() {
		return this.over;
	}
	
	/**
	 * 是否继续处理
	 */
	public boolean next() {
		return this.next;
	}

	/**
	 * 填充
	 */
	private byte[] buildPadding(int maxLength) {
		final Random random = new Random();
		final byte[] padding = new byte[random.nextInt(maxLength + 1)];
		for (int index = 0; index < padding.length; index++) {
			padding[index] = (byte) random.nextInt(SystemConfig.UNSIGNED_BYTE_SIZE);
		}
		return padding;
	}

	/**
	 * 发起握手
	 */
	public void handshake() {
		sendPublicKey();
	}

	/**
	 * 握手
	 */
	public void handshake(ByteBuffer buffer) {
		switch (this.step) {
		case 0:
			receivePublicKey(buffer);
			if(!this.over) {
				sendPublicKey();
			}
			break;
		case 1:
			receivePublicKey(buffer);
			break;
		default:
			break;
		}
	}

	private void sendPublicKey() {
		final KeyPair keyPair = this.builder.buildKeyPair();
		final byte[] publicKey = keyPair.getPublic().getEncoded();
		final byte[] padding = buildPadding(CryptoConfig.MSE_MAX_PADDING);
		final ByteBuffer buffer = ByteBuffer.allocate(publicKey.length + padding.length);
		buffer.put(publicKey);
		buffer.put(padding);
		buffer.flip();
		this.peerSubMessageHandler.send(buffer);
		this.step++;
	}

	private void receivePublicKey(ByteBuffer buffer) {
		final byte first = buffer.get();
		if(first == PeerConfig.HANDSHAKE_NAME_LENGTH) { // 明文
			final byte[] names = new byte[PeerConfig.HANDSHAKE_NAME_LENGTH];
			buffer.get(names);
			if(ArrayUtils.equals(names, PeerConfig.HANDSHAKE_NAME_BYTES)) { // 明文
				buffer.position(0);
				this.over = true;
				this.next = true;
				this.crypt = false;
				return;
			}
		}
		buffer.position(0);
	}
	
	/**
	 * 加密
	 */
	public void encrypt(ByteBuffer buffer) {
		
	}

	/**
	 * 解密
	 */
	public void decrypt(ByteBuffer buffer) {
		
	}

}
