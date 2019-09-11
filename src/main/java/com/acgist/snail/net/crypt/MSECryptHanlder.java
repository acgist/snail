package com.acgist.snail.net.crypt;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.PeerUnpackMessageHandler;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerSubMessageHandler;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.system.config.CryptConfig;
import com.acgist.snail.system.config.CryptConfig.Strategy;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.DigestUtils;
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
	 * 是否处理完成（握手处理）
	 */
	private volatile boolean over = false;
	/**
	 * 是否加密：默认明文
	 */
	private volatile boolean crypt = false;
	/**
	 * 密钥
	 */
	private volatile MSECipher cipher;
	
	private final PeerSubMessageHandler peerSubMessageHandler;
	private final PeerUnpackMessageHandler peerUnpackMessageHandler;
	
	/**
	 * 握手等待锁
	 */
	private final Object handshakeLock = new Object();
	
	// 握手临时数据，握手完成后销毁
	private KeyPair keyPair; // 密钥对
	private ByteBuffer buffer; // 数据缓冲
	private BigInteger dhSecret; // S：DH Secret
	private MSEKeyPairBuilder mseKeyPairBuilder; // 密钥对Builder

	private MSECryptHanlder(PeerSubMessageHandler peerSubMessageHandler, PeerUnpackMessageHandler peerUnpackMessageHandler) {
		final MSEKeyPairBuilder mseKeyPairBuilder = MSEKeyPairBuilder.newInstance();
		this.buffer = ByteBuffer.allocate(4096); // 4KB
		this.keyPair = mseKeyPairBuilder.buildKeyPair();
		this.mseKeyPairBuilder = mseKeyPairBuilder;
		this.peerSubMessageHandler = peerSubMessageHandler;
		this.peerUnpackMessageHandler = peerUnpackMessageHandler;
	}

	public static final MSECryptHanlder newInstance(PeerSubMessageHandler peerSubMessageHandler, PeerUnpackMessageHandler peerUnpackMessageHandler) {
		return new MSECryptHanlder(peerSubMessageHandler, peerUnpackMessageHandler);
	}

	/**
	 * 是否处理完成
	 */
	public boolean over() {
		return this.over;
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
		try {
			if(checkPeerHandshake(buffer)) { // Peer握手消息
				LOGGER.debug("跳过加密握手，直接进行Peer握手。");
				return;
			}
			switch (this.step) {
			case 1:
			case 2:
				receivePublicKey(buffer);
				break;
			case 3:
				receiveProvide(buffer);
				break;
			case 4:
				receiveConfirm(buffer);
				break;
			default:
				LOGGER.warn("不支持的加密握手步骤：{}", this.step);
				break;
			}
		} catch (Exception e) {
			this.plaintext();
			LOGGER.error("加密握手异常", e);
		}
	}
	
	/**
	 * 设置明文
	 */
	public void plaintext() {
		this.over(true, false);
	}
	
	/**
	 * 加密
	 */
	public void encrypt(ByteBuffer buffer) {
		encrypt(buffer, this.crypt);
	}

	private void encrypt(ByteBuffer buffer, boolean crypt) {
		if(crypt) {
			synchronized (this.cipher) {
				try {
					boolean flip = true;
					if(buffer.position() != 0) { //  重置标记
						flip = false;
						buffer.flip();
					}
					final byte[] value = new byte[buffer.remaining()];
					buffer.get(value);
					final byte[] eValue = this.cipher.getEncryptionCipher().update(value);
					buffer.clear().put(eValue);
					if(flip) {
						buffer.flip();
					}
				} catch (Exception e) {
					LOGGER.error("加密异常", e);
				}
			}
		}
	}
	
	/**
	 * 解密
	 */
	public void decrypt(ByteBuffer buffer) {
		decrypt(buffer, this.crypt);
	}
	
	public void decrypt(ByteBuffer buffer, boolean crypt) {
		if(crypt) {
			try {
				boolean flip = true;
				if(buffer.position() != 0) { //  重置标记
					flip = false;
					buffer.flip();
				}
				final byte[] value = new byte[buffer.remaining()];
				buffer.get(value);
				final byte[] dValue = this.cipher.getDecryptionCipher().update(value);
				buffer.clear().put(dValue);
				if(flip) {
					buffer.flip();
				}
			} catch (Exception e) {
				LOGGER.error("解密异常", e);
			}
		}
	}
	
	/**
	 * 握手等待锁：5秒
	 */
	public void handshakeLock() {
		if(!this.over) {
			synchronized (this.handshakeLock) {
				if(!this.over) {
					ThreadUtils.wait(this.handshakeLock, Duration.ofSeconds(5));
				}
			}
		}
		if(!this.over) { // 握手没有完成：明文
			this.plaintext();
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
		LOGGER.debug("加密握手，发送公钥，步骤：{}", this.step);
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
	private void receivePublicKey(ByteBuffer buffer) throws NetException, UnsupportedEncodingException {
		LOGGER.debug("加密握手，接收公钥，步骤：{}", this.step);
		this.buffer.put(buffer); // 读取信息
		final int minLength = CryptConfig.PUBLIC_KEY_SIZE; // 最短读取数据长度
		final int maxLength = minLength + CryptConfig.MSE_MAX_PADDING; // 最大读取数据长度
		if(this.buffer.position() < minLength) { // 不够公钥长度继续读取
			return;
		}
		if(this.buffer.position() > maxLength) { // 数据超过最大长度
			throw new NetException("加密握手长度错误");
		}
		this.buffer.flip();
		final BigInteger publicKey = NumberUtils.decodeUnsigned(this.buffer, minLength);
		this.dhSecret = this.mseKeyPairBuilder.buildDHSecret(publicKey, this.keyPair.getPrivate());
		this.buffer.clear();
		if(this.step == 1) {
			sendPublicKey();
			this.step = 3;
		} else if(this.step == 2) {
			sendProvide();
			this.step = 4;
		}
	}
	
	/**
	 * 提供加密选择：
	 * HASH('req1', S),
	 * HASH('req2', SKEY) xor HASH('req3', S),
	 * ENCRYPT(VC, crypto_provide, len(PadC), PadC, len(IA)),
	 * ENCRYPT(IA)
	 */
	private void sendProvide() throws NetException, UnsupportedEncodingException {
		LOGGER.debug("加密握手，提供加密选择，步骤：{}", this.step);
		final TorrentSession torrentSession = this.peerSubMessageHandler.torrentSession();
		if(torrentSession == null) {
			throw new NetException("加密握手TorrentSession为空");
		}
		final byte[] dhSecretBytes = NumberUtils.encodeUnsigned(this.dhSecret, CryptConfig.PUBLIC_KEY_SIZE);
		final InfoHash infoHash = torrentSession.infoHash();
		this.cipher = MSECipher.newInitiator(dhSecretBytes, infoHash); // 加密套件
		ByteBuffer buffer = ByteBuffer.allocate(40); // 20 + 20
		final MessageDigest digest = DigestUtils.sha1();
//		HASH('req1', S)
		digest.update("req1".getBytes(SystemConfig.CHARSET_ASCII));
		digest.update(dhSecretBytes);
		buffer.put(digest.digest());
//		HASH('req2', SKEY) xor HASH('req3', S)
		digest.update("req2".getBytes(SystemConfig.CHARSET_ASCII));
		digest.update(infoHash.infoHash());
		final byte[] req2 = digest.digest();
		digest.update("req3".getBytes(SystemConfig.CHARSET_ASCII));
		digest.update(dhSecretBytes);
		final byte[] req3 = digest.digest();
		buffer.put(ArrayUtils.xor(req2, req3));
		this.peerSubMessageHandler.send(buffer);
//		ENCRYPT(VC, crypto_provide, len(PadC), PadC, len(IA))
		final byte[] padding = buildZeroPadding(CryptConfig.MSE_MAX_PADDING);
		final int paddingLength = padding.length;
		buffer = ByteBuffer.allocate(16 + paddingLength); // 8 + 4 + 2 + Padding + 2
		buffer.put(CryptConfig.VC); // VC
		buffer.putInt(CryptConfig.STRATEGY.provide()); // crypto_provide
		buffer.putShort((short) paddingLength); // len(PadC)
		buffer.put(padding); // PadC
		buffer.putShort((short) 0); // len(IA)
		this.encrypt(buffer, true);
		this.peerSubMessageHandler.send(buffer);
	}

	/**
	 * 接收加密选择
	 */
	private void receiveProvide(ByteBuffer buffer) throws NetException, UnsupportedEncodingException {
		LOGGER.debug("加密握手，接收加密选择，步骤：{}", this.step);
		this.buffer.put(buffer);
		final int minLength = 20 + 20 + 8 + 4 + 2 + 0 + 2;
		final int maxLength = 20 + 20 + 8 + 4 + 2 + 512 + 2;
		if(this.buffer.position() < minLength) {
			return;
		}
		if(this.buffer.position() > maxLength) {
			throw new NetException("加密握手长度错误");
		}
		this.buffer.flip();
		final MessageDigest digest = DigestUtils.sha1();
		final byte[] dhSecretBytes = NumberUtils.encodeUnsigned(this.dhSecret, CryptConfig.PUBLIC_KEY_SIZE);
		final byte[] req1 = new byte[20];
		final byte[] req2x3 = new byte[20];
//		HASH('req1', S)
		digest.update("req1".getBytes(SystemConfig.CHARSET_ASCII));
		digest.update(dhSecretBytes);
		final byte[] req1Native = digest.digest();
		this.buffer.get(req1);
		if(!ArrayUtils.equals(req1, req1Native)) {
			throw new NetException("加密握手req1不一致");
		}
		ArrayUtils.equals(req1, req1Native);
//		HASH('req2', SKEY) xor HASH('req3', S)
		this.buffer.get(req2x3);
		digest.update("req3".getBytes(SystemConfig.CHARSET_ASCII));
		digest.update(dhSecretBytes);
		InfoHash infoHash = null;
		final byte[] req3 = digest.digest();
		for (InfoHash tmp : TorrentManager.getInstance().allInfoHash()) {
			digest.update("req2".getBytes(SystemConfig.CHARSET_ASCII));
			digest.update(tmp.infoHash());
			byte[] req2 = digest.digest();
			if (ArrayUtils.equals(ArrayUtils.xor(req2, req3), req2x3)) {
				infoHash = tmp;
				break;
			}
		}
		if(infoHash == null) {
			throw new NetException("加密握手不存在的种子信息");
		}
		this.cipher = MSECipher.newReceiver(dhSecretBytes, infoHash);
//		ENCRYPT(VC, crypto_provide, len(PadC), PadC, len(IA))
		ByteBuffer encryptBuffer = ByteBuffer.allocate(this.buffer.remaining());
		encryptBuffer.put(this.buffer);
		encryptBuffer.flip();
		this.decrypt(encryptBuffer, true);
		final byte[] vc = new byte[CryptConfig.VC_LENGTH];
		encryptBuffer.get(vc);
		final int provide = encryptBuffer.getInt(); // 协议选择
		final Strategy strategy = selectStrategy(provide); // 选择协议
		sendConfirm(strategy);
	}
	
	/**
	 * 确认加密
	 * ENCRYPT(VC, crypto_select, len(padD), padD),
	 * ENCRYPT2(Payload Stream)
	 */
	private void sendConfirm(Strategy strategy) {
		LOGGER.debug("加密握手，确认加密，步骤：{}", this.step);
		final byte[] padding = buildZeroPadding(CryptConfig.MSE_MAX_PADDING);
		final int paddingLength = padding.length;
		final ByteBuffer buffer = ByteBuffer.allocate(14 + paddingLength);
		buffer.put(CryptConfig.VC);
		buffer.putInt(strategy.provide());
		buffer.putShort((short) paddingLength);
		buffer.put(padding);
		this.encrypt(buffer, true);
		this.peerSubMessageHandler.send(buffer);
		LOGGER.debug("发送确认加密协议：{}", strategy);
		this.over(true, strategy.crypt());
	}
	
	/**
	 * 确认加密
	 */
	private void receiveConfirm(ByteBuffer buffer) throws NetException {
		LOGGER.debug("加密握手，确认加密，步骤：{}", this.step);
		this.buffer.put(buffer);
		final int minLength = 8 + 4 + 2 + 0;
		final int maxLength = 8 + 4 + 2 + 512;
		if(this.buffer.position() < minLength) {
			return;
		}
		if(this.buffer.position() > maxLength) {
			throw new NetException("加密握手长度错误");
		}
		this.buffer.flip();
		this.decrypt(this.buffer, true);
		final byte[] vc = new byte[CryptConfig.VC_LENGTH];
		this.buffer.get(vc);
		final int provide = this.buffer.getInt(); // 协议选择
		final Strategy strategy = selectStrategy(provide); // 选择协议
		LOGGER.debug("接收确认加密协议：{}", strategy);
		this.over(true, strategy.crypt());
	}
	
	/**
	 * 选择策略
	 */
	private CryptConfig.Strategy selectStrategy(int provide) throws NetException {
		final boolean plaintext = (provide & 0x01) == 0x01;
		final boolean crypt = 	  (provide & 0x02) == 0x02;
		Strategy selected = null;
		if (plaintext || crypt) {
			switch (CryptConfig.STRATEGY) { // 本地配置
			case plaintext:
				selected = plaintext ? Strategy.plaintext : null;
				break;
			case preferPlaintext:
				selected = plaintext ? Strategy.plaintext : Strategy.encrypt;
				break;
			case preferEncrypt:
				selected = crypt ? Strategy.encrypt : Strategy.plaintext;
				break;
			case encrypt:
				selected = crypt ? Strategy.encrypt : null;
				break;
			}
		}
		if (selected == null) {
			throw new NetException("不支持加密协议：" + provide);
		}
		return selected;
	}
	
	/**
	 * 填充：随机值
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
	 * 填充：0
	 */
    private byte[] buildZeroPadding(int maxLength) {
        final Random random = new Random();
        return new byte[random.nextInt(maxLength + 1)];
    }
	
    /**
     * Peer握手
     */
	private boolean checkPeerHandshake(ByteBuffer buffer) throws NetException {
		// 先判断是否是握手消息
		final byte first = buffer.get();
		if(first == PeerConfig.HANDSHAKE_NAME_LENGTH) {
			final byte[] names = new byte[PeerConfig.HANDSHAKE_NAME_LENGTH];
			buffer.get(names);
			if(ArrayUtils.equals(names, PeerConfig.HANDSHAKE_NAME_BYTES)) { // 握手消息直接使用明文
				this.over(true, false);
				buffer.position(0); // 重置长度
				this.peerUnpackMessageHandler.onMessage(buffer);
				return true;
			}
		}
		buffer.position(0); // 重置长度
		return false;
	}
    
	/**
	 * 完成
	 * 
	 * @param over 是否完成
	 * @param crypt 是否加密
	 */
	private void over(boolean over, boolean crypt) {
		this.over = over;
		this.crypt = crypt;
		this.buffer = null;
		this.keyPair = null;
		this.dhSecret = null;
		this.mseKeyPairBuilder = null;
		this.unHandshakeLock();
	}

}
