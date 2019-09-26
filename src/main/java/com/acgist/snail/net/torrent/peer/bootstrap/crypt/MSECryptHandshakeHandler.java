package com.acgist.snail.net.torrent.peer.bootstrap.crypt;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.peer.PeerUnpackMessageCodec;
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
 * <p>MSE握手代理</p>
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
 * TODO：粘包拆包处理
 * 
 * @author acgist
 * @since 1.1.0
 */
public class MSECryptHandshakeHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MSECryptHandshakeHandler.class);

	/**
	 * 缓冲区大小：4KB
	 */
	private static final int BUFFER_SIZE = 4096;
	
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
	 * 握手等待锁
	 */
	private final Object handshakeLock = new Object();
	/**
	 * 加密套件
	 */
	private volatile MSECipher cipher;
	/**
	 * 加密套件临时
	 */
	private volatile MSECipher cipherTmp;
	
	private final PeerSubMessageHandler peerSubMessageHandler;
	private final PeerUnpackMessageCodec peerUnpackMessageCodec;
	
	/**
	 * 密钥对
	 */
	private KeyPair keyPair;
	/**
	 * 数据缓冲
	 */
	private ByteBuffer buffer;
	/**
	 * S：DH Secret
	 */
	private BigInteger dhSecret;
	/**
	 * 密钥对Builder
	 */
	private MSEKeyPairBuilder mseKeyPairBuilder;

	private MSECryptHandshakeHandler(PeerSubMessageHandler peerSubMessageHandler, PeerUnpackMessageCodec peerUnpackMessageCodec) {
		final MSEKeyPairBuilder mseKeyPairBuilder = MSEKeyPairBuilder.newInstance();
		this.buffer = ByteBuffer.allocate(BUFFER_SIZE);
		this.keyPair = mseKeyPairBuilder.buildKeyPair();
		this.mseKeyPairBuilder = mseKeyPairBuilder;
		this.peerSubMessageHandler = peerSubMessageHandler;
		this.peerUnpackMessageCodec = peerUnpackMessageCodec;
	}

	public static final MSECryptHandshakeHandler newInstance(PeerSubMessageHandler peerSubMessageHandler, PeerUnpackMessageCodec peerUnpackMessageCodec) {
		return new MSECryptHandshakeHandler(peerSubMessageHandler, peerUnpackMessageCodec);
	}

	/**
	 * 是否处理完成
	 */
	public boolean over() {
		return this.over;
	}

	/**
	 * 发送握手消息
	 */
	public void handshake() {
		sendPublicKey();
		this.step = 2;
	}

	/**
	 * 处理握手消息
	 */
	public void handshake(ByteBuffer buffer) {
		try {
			if(checkPeerHandshake(buffer)) { // Peer握手消息
				LOGGER.debug("跳过加密握手，直接进行Peer握手。");
				return;
			}
			synchronized (this.buffer) {
				this.buffer.put(buffer); // 读取信息
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
	 * 加密，不改变buffer读取和写入状态。
	 */
	public void encrypt(ByteBuffer buffer) {
		if(this.crypt) {
			this.cipher.encrypt(buffer);
		}
	}
	
	/**
	 * 解密，不改变buffer读取和写入状态。
	 */
	public void decrypt(ByteBuffer buffer) {
		if(this.crypt) {
			this.cipher.decrypt(buffer);
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
		final byte[] padding = buildPadding(CryptConfig.PADDING_MAX_LENGTH);
		final ByteBuffer buffer = ByteBuffer.allocate(publicKey.length + padding.length);
		buffer.put(publicKey);
		buffer.put(padding);
		this.peerSubMessageHandler.send(buffer);
	}

	private static final int PUBLIC_KEY_MIN_LENGTH = CryptConfig.PUBLIC_KEY_LENGTH; // 最短读取数据长度
	private static final int PUBLIC_KEY_MAX_LENGTH = PUBLIC_KEY_MIN_LENGTH + CryptConfig.PADDING_MAX_LENGTH; // 最大读取数据长度
	
	/**
	 * 接收公钥
	 */
	private void receivePublicKey(ByteBuffer buffer) throws NetException {
		LOGGER.debug("加密握手，接收公钥，步骤：{}", this.step);
		if(this.buffer.position() < PUBLIC_KEY_MIN_LENGTH) { // 不够公钥长度继续读取
			return;
		}
		if(this.buffer.position() > PUBLIC_KEY_MAX_LENGTH) { // 数据超过最大长度
			throw new NetException("加密握手长度错误");
		}
		this.buffer.flip();
		final BigInteger publicKey = NumberUtils.decodeUnsigned(this.buffer, CryptConfig.PUBLIC_KEY_LENGTH);
		this.buffer.compact();
		this.dhSecret = this.mseKeyPairBuilder.buildDHSecret(publicKey, this.keyPair.getPrivate());
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
	private void sendProvide() throws NetException {
		LOGGER.debug("加密握手，提供加密选择，步骤：{}", this.step);
		final TorrentSession torrentSession = this.peerSubMessageHandler.torrentSession();
		if(torrentSession == null) {
			throw new NetException("加密握手TorrentSession为空");
		}
		final byte[] dhSecretBytes = NumberUtils.encodeUnsigned(this.dhSecret, CryptConfig.PUBLIC_KEY_LENGTH);
		final InfoHash infoHash = torrentSession.infoHash();
		this.cipher = MSECipher.newInitiator(dhSecretBytes, infoHash); // 加密套件
		this.cipherTmp = MSECipher.newInitiator(dhSecretBytes, infoHash); // 加密套件临时
		ByteBuffer buffer = ByteBuffer.allocate(40); // 20 + 20
		final MessageDigest digest = DigestUtils.sha1();
//		HASH('req1', S)
		digest.update("req1".getBytes());
		digest.update(dhSecretBytes);
		buffer.put(digest.digest());
//		HASH('req2', SKEY) xor HASH('req3', S)
		digest.update("req2".getBytes());
		digest.update(infoHash.infoHash());
		final byte[] req2 = digest.digest();
		digest.update("req3".getBytes());
		digest.update(dhSecretBytes);
		final byte[] req3 = digest.digest();
		buffer.put(ArrayUtils.xor(req2, req3));
		this.peerSubMessageHandler.send(buffer);
//		ENCRYPT(VC, crypto_provide, len(PadC), PadC, len(IA))
		final byte[] padding = buildZeroPadding(CryptConfig.PADDING_MAX_LENGTH);
		final int paddingLength = padding.length;
		buffer = ByteBuffer.allocate(16 + paddingLength); // 8 + 4 + 2 + Padding + 2 + 0
		buffer.put(CryptConfig.VC); // VC
		buffer.putInt(CryptConfig.STRATEGY.provide()); // crypto_provide
		buffer.putShort((short) paddingLength); // len(PadC)
		buffer.put(padding); // PadC
		buffer.putShort((short) 0); // len(IA)
		this.cipher.encrypt(buffer);
		this.peerSubMessageHandler.send(buffer);
	}

	private static final int PROVIDE_MIN_LENGTH = 20 + 20 + 8 + 4 + 2 + 0 + 2 + 0;
	private static final int PROVIDE_MAX_LENGTH = PROVIDE_MIN_LENGTH + CryptConfig.PADDING_MAX_LENGTH;
	
	/**
	 * 接收加密选择
	 */
	private void receiveProvide(ByteBuffer buffer) throws NetException {
		LOGGER.debug("加密握手，接收加密选择，步骤：{}", this.step);
		final MessageDigest digest = DigestUtils.sha1();
		final byte[] dhSecretBytes = NumberUtils.encodeUnsigned(this.dhSecret, CryptConfig.PUBLIC_KEY_LENGTH);
//		HASH('req1', S)
		digest.update("req1".getBytes());
		digest.update(dhSecretBytes);
		final byte[] req1Native = digest.digest();
		if(!match(req1Native)) {
			return;
		}
		if(this.buffer.position() < PROVIDE_MIN_LENGTH) {
			return;
		}
		if(this.buffer.position() > PROVIDE_MAX_LENGTH) {
			throw new NetException("加密握手长度错误");
		}
		this.buffer.flip();
		final byte[] req1 = new byte[20];
		this.buffer.get(req1);
//		HASH('req2', SKEY) xor HASH('req3', S)
		final byte[] req2x3 = new byte[20];
		this.buffer.get(req2x3);
		digest.update("req3".getBytes());
		digest.update(dhSecretBytes);
		InfoHash infoHash = null;
		final byte[] req3 = digest.digest();
		for (InfoHash tmp : TorrentManager.getInstance().allInfoHash()) {
			digest.update("req2".getBytes());
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
		final ByteBuffer encryptBuffer = ByteBuffer.allocate(this.buffer.remaining());
		encryptBuffer.put(this.buffer);
		encryptBuffer.flip();
		this.cipher.decrypt(encryptBuffer);
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
		LOGGER.debug("加密握手，发送确认加密，步骤：{}", this.step);
		final byte[] padding = buildZeroPadding(CryptConfig.PADDING_MAX_LENGTH);
		final int paddingLength = padding.length;
		final ByteBuffer buffer = ByteBuffer.allocate(14 + paddingLength); // 8 + 4 + 2 + Padding
		buffer.put(CryptConfig.VC);
		buffer.putInt(strategy.provide());
		buffer.putShort((short) paddingLength);
		buffer.put(padding);
		this.cipher.encrypt(buffer);
		this.peerSubMessageHandler.send(buffer);
		LOGGER.debug("发送确认加密协议：{}", strategy);
		this.over(true, strategy.crypt());
	}
	
	private static final int CONFIRM_MIN_LENGTH = 8 + 4 + 2 + 0;
	private static final int CONFIRM_MAX_LENGTH = CONFIRM_MIN_LENGTH + CryptConfig.PADDING_MAX_LENGTH;
	
	/**
	 * 确认加密
	 */
	private void receiveConfirm(ByteBuffer buffer) throws NetException {
		LOGGER.debug("加密握手，收到确认加密，步骤：{}", this.step);
		final byte[] vcMatch = this.cipherTmp.decrypt(CryptConfig.VC);
		if(!match(vcMatch)) {
			return;
		}
		if(this.buffer.position() < CONFIRM_MIN_LENGTH) {
			return;
		}
		if(this.buffer.position() > CONFIRM_MAX_LENGTH) {
			throw new NetException("加密握手长度错误");
		}
//		ENCRYPT(VC, crypto_select, len(padD), padD)
		this.buffer.flip();
		this.cipher.decrypt(this.buffer);
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
		LOGGER.debug("选择策略：{}", provide);
		final boolean plaintext = (provide & 0x01) == 0x01;
		final boolean crypt = 	  (provide & 0x02) == 0x02;
		Strategy selected = null;
		if (plaintext || crypt) {
			// 本地配置
			switch (CryptConfig.STRATEGY) {
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
			default:
				selected = CryptConfig.STRATEGY.crypt() ? Strategy.encrypt : Strategy.plaintext;
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
		final Random random = NumberUtils.random();
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
        final Random random = NumberUtils.random();
        return new byte[random.nextInt(maxLength + 1)];
    }
	
    /**
     * Peer握手
     */
	private boolean checkPeerHandshake(ByteBuffer buffer) throws NetException {
		// 先判断是否是握手消息
		final byte first = buffer.get();
		if(first == PeerConfig.HANDSHAKE_NAME_LENGTH && buffer.remaining() >= PeerConfig.HANDSHAKE_NAME_LENGTH) {
			final byte[] names = new byte[PeerConfig.HANDSHAKE_NAME_LENGTH];
			buffer.get(names);
			if(ArrayUtils.equals(names, PeerConfig.HANDSHAKE_NAME_BYTES)) { // 握手消息直接使用明文
				this.over(true, false);
				buffer.position(0); // 重置长度
				this.peerUnpackMessageCodec.decode(buffer);
				return true;
			}
		}
		buffer.position(0); // 重置长度
		return false;
	}
	
	/**
	 * 数据匹配
	 * 
	 * @param bytes 匹配数据
	 */
	private boolean match(byte[] bytes) {
		int index = 0;
		final int length = bytes.length;
		this.buffer.flip();
		if(this.buffer.remaining() < length) {
			this.buffer.compact();
			return false;
		}
		while(length > index) {
			if(this.buffer.get() != bytes[index]) {
				this.buffer.position(this.buffer.position() - index); // 最开始的位置移动一位继续匹配
				index = 0; // 注意位置
				if(this.buffer.remaining() < length) {
					break;
				}
			} else {
				index++;
			}
		}
		if(index == length) { // 匹配
			this.buffer.position(this.buffer.position() - length); // 匹配值还原
			this.buffer.compact();
			return true;
		} else { // 不匹配
			this.buffer.compact();
			return false;
		}
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
		this.cipherTmp = null;
		this.mseKeyPairBuilder = null;
		this.unHandshakeLock();
	}

}
