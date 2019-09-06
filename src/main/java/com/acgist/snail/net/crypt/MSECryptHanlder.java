package com.acgist.snail.net.crypt;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.time.Duration;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.peer.bootstrap.PeerSubMessageHandler;
import com.acgist.snail.system.config.CryptConfig;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.NumberUtils;
import com.acgist.snail.utils.ThreadUtils;

/**
 * <p>MSE密钥工具</p>
 * <p>加密算法：ARC4</p>
 * <p>密钥交换算法：DH(Diffie-Hellman)</p>
 * <p>参考链接：https://wiki.vuze.com/w/Message_Stream_Encryption</p>
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
 * <p>SKEY：infoHash</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class MSECryptHanlder {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MSECryptHanlder.class);

	/**
	 * 当前步骤：默认接收
	 */
	private volatile int step = 1;
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
	
	private final PeerSubMessageHandler peerSubMessageHandler;
	
	/**
	 * 握手等待锁
	 */
	private final Object handshakeLock = new Object();
	
	// 握手临时数据，握手完成后销毁
	private KeyPair keyPair; // 密钥对
	private ByteBuffer buffer; // 数据缓冲
	private BigInteger dhSecret; // S：DH Secret
	private MSEKeyPairBuilder mseKeyPairBuilder; // 密钥对Builder

	private MSECryptHanlder(PeerSubMessageHandler peerSubMessageHandler) {
		final MSEKeyPairBuilder mseKeyPairBuilder = MSEKeyPairBuilder.newInstance();
		this.buffer = ByteBuffer.allocate(1024);
		this.keyPair = mseKeyPairBuilder.buildKeyPair();
		this.mseKeyPairBuilder = mseKeyPairBuilder;
		this.peerSubMessageHandler = peerSubMessageHandler;
	}

	public static final MSECryptHanlder newInstance(PeerSubMessageHandler peerSubMessageHandler) {
		return new MSECryptHanlder(peerSubMessageHandler);
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
	 * 发起握手
	 */
	public void handshake() {
		sendPublicKey();
		this.step = 2;
	}

	/**
	 * 握手
	 */
	public void handshake(ByteBuffer buffer) {
		switch (this.step) {
		case 1:
		case 2:
			receivePublicKey(buffer);
			break;
		default:
			LOGGER.warn("不支持的加密握手步骤：{}", this.step);
			break;
		}
	}
	
	/**
	 * 设置明文
	 */
	public void plaintext() {
		this.over(true, false, false);
	}
	
	/**
	 * 加密
	 */
	public void encrypt(ByteBuffer buffer) {
		if(this.crypt) {
		}
	}

	/**
	 * 解密
	 */
	public void decrypt(ByteBuffer buffer) {
		if(this.crypt) {
		}
	}
	
	/**
	 * 握手等待锁：5秒
	 */
	public void handshakeLock() {
		synchronized (this.handshakeLock) {
			ThreadUtils.wait(this.handshakeLock, Duration.ofSeconds(5));
		}
		if(!this.over) { // 握手没有完成：明文
			this.over(true, false, false);
		}
	}
	
	/**
	 * 唤醒握手等待
	 */
	private void unHandshakeLock() {
		synchronized (this.handshakeLock) {
			this.handshakeLock.notifyAll();
		}
	}

	/**
	 * 发送公钥
	 */
	private void sendPublicKey() {
		final byte[] publicKey = this.keyPair.getPublic().getEncoded();
		final byte[] padding = buildPadding(CryptConfig.MSE_MAX_PADDING);
		final ByteBuffer buffer = ByteBuffer.allocate(publicKey.length + padding.length);
		buffer.put(publicKey);
		buffer.put(padding);
		buffer.flip();
		this.peerSubMessageHandler.send(buffer);
	}

	/**
	 * 接收公钥
	 */
	private void receivePublicKey(ByteBuffer buffer) {
		// 先判断是否是握手消息
		final byte first = buffer.get();
		if(first == PeerConfig.HANDSHAKE_NAME_LENGTH) {
			final byte[] names = new byte[PeerConfig.HANDSHAKE_NAME_LENGTH];
			buffer.get(names);
			if(ArrayUtils.equals(names, PeerConfig.HANDSHAKE_NAME_BYTES)) { // 握手消息直接使用明文
				buffer.position(0); // 重置长度
				this.over(true, true, false);
				return;
			}
		}
		buffer.position(0); // 重置长度
		this.buffer.put(buffer);
		if(this.buffer.position() < CryptConfig.PUBLIC_KEY_SIZE) { // 不够公钥长度继续读取
			return;
		}
		final int minSize = CryptConfig.PUBLIC_KEY_SIZE; // 最短读取数据长度
//		final int maxSize = minSize + CryptConfig.MSE_MAX_PADDING; // 最大读取数据长度
		final BigInteger publicKey = NumberUtils.decodeUnsigned(this.buffer, minSize);
		this.dhSecret = this.mseKeyPairBuilder.buildDHSecret(publicKey, this.keyPair.getPrivate());
		if(this.step == 1) {
			sendPublicKey();
			this.step = 3;
		} else if(this.step == 2) {
			provide();
			this.step = 4;
		}
	}
	
	/**
	 * 提供加密选择：
	 * HASH('req1', S),
	 * HASH('req2', SKEY) xor HASH('req3', S),
	 * ENCRYPT(VC, crypto_provide, len(PadC), PadC, len(IA)), ENCRYPT(IA)
	 */
	private void provide() {
//		final InfoHash infoHash = null; // TODO
//		final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//		final MessageDigest digest = DigestUtils.sha1();
////		HASH('req1', S)
//		digest.update("req1".getBytes(SystemConfig.CHARSET_ASCII));
//		digest.update(NumberUtils.encodeUnsigned(this.dhSecret, CryptConfig.PUBLIC_KEY_SIZE));
//		bytes.write(digest.digest());
////		HASH('req2', SKEY) xor HASH('req3', S)
//		digest.update("req2".getBytes(SystemConfig.CHARSET_ASCII));
//		digest.update(infoHash.infoHash());
//		byte[] req2 = digest.digest();
//		digest.update("req3".getBytes(SystemConfig.CHARSET_ASCII));
//		digest.update(NumberUtils.encodeUnsigned(this.dhSecret, CryptConfig.PUBLIC_KEY_SIZE));
//		byte[] req3 = digest.digest();
//		bytes.write(ArrayUtils.xor(req2, req3));
//		// TODO：write
//        final byte[] Sbytes = NumberUtils.encodeUnsigned(this.dhSecret, CryptConfig.PUBLIC_KEY_SIZE);
//        final MSECipher cipher = MSECipher.newInitiator(Sbytes, infoHash); // 加密套件
//        // - ENCRYPT(VC, crypto_provide, len(PadC), PadC, len(IA))
//        bytes.reset();
//        bytes.write(CryptConfig.VC);
//        // TODO：
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
	 * 完成
	 */
	private void over(boolean over, boolean next, boolean crypt) {
		this.over = over;
		this.next = next;
		this.crypt = crypt;
		this.buffer = null;
	}

}
