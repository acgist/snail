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
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.ThreadUtils;

/**
 * <p>MSE握手代理</p>
 * <p>加密算法：ARC4</p>
 * <p>密钥交换算法：DH（Diffie-Hellman）</p>
 * <p>协议链接：https://wiki.vuze.com/w/Message_Stream_Encryption</p>
 * <p>协议链接：https://wiki.openssl.org/index.php/Diffie_Hellman</p>
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
 * <p>SKEY：InfoHash</p>
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
	 * 握手等待超时时间
	 */
	private static final int HANDSHAKE_LOCK_TIMEOUT = 5;
	
	/**
	 * 握手步骤
	 */
	public enum Step {
		
		/** 发送公钥 */
		sendPublicKey,
		/** 接收公钥 */
		receivePublicKey,
		/** 发送加密协议协商 */
		sendProvide,
		/** 接收加密协议协商 */
		receiveProvide,
		receiveProvidePadding,
		/** 发送确认加密协议 */
		sendConfirm,
		/** 接收确认加密协议 */
		receiveConfirm,
		receiveConfirmPadding;
		
	}
	
	/**
	 * 当前步骤：默认：接收公钥
	 */
	private volatile Step step = Step.receivePublicKey;
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
	private MSECipher cipher;
	/**
	 * 临时加密套件：VC数据查找时使用
	 */
	private MSECipher cipherTmp;
	/**
	 * 密钥对
	 */
	private KeyPair keyPair;
	/**
	 * 加密策略
	 */
	private Strategy strategy;
	/**
	 * 数据缓冲
	 */
	private ByteBuffer buffer;
	/**
	 * S：DH Secret
	 */
	private BigInteger dhSecret;
	/**
	 * Padding数据读取器
	 */
	private MSEPaddingSync msePaddingSync;
	
	private final PeerSubMessageHandler peerSubMessageHandler;
	private final PeerUnpackMessageCodec peerUnpackMessageCodec;

	private MSECryptHandshakeHandler(PeerUnpackMessageCodec peerUnpackMessageCodec, PeerSubMessageHandler peerSubMessageHandler) {
		final MSEKeyPairBuilder mseKeyPairBuilder = MSEKeyPairBuilder.newInstance();
		this.buffer = ByteBuffer.allocate(BUFFER_SIZE);
		this.keyPair = mseKeyPairBuilder.buildKeyPair();
		this.peerSubMessageHandler = peerSubMessageHandler;
		this.peerUnpackMessageCodec = peerUnpackMessageCodec;
	}

	public static final MSECryptHandshakeHandler newInstance(PeerUnpackMessageCodec peerUnpackMessageCodec, PeerSubMessageHandler peerSubMessageHandler) {
		return new MSECryptHandshakeHandler(peerUnpackMessageCodec, peerSubMessageHandler);
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
		this.step = Step.sendPublicKey;
		sendPublicKey();
	}

	/**
	 * 处理握手消息
	 */
	public void handshake(ByteBuffer buffer) throws NetException {
		try {
			if(checkPeerHandshake(buffer)) {
				LOGGER.debug("加密握手（跳过）：收到Peer握手消息");
				return;
			}
			synchronized (this.buffer) {
				switch (this.step) {
				case sendPublicKey:
				case receivePublicKey:
					this.buffer.put(buffer);
					receivePublicKey();
					break;
				case receiveProvide:
					this.buffer.put(buffer);
					receiveProvide();
					break;
				case receiveProvidePadding:
					this.cipher.decrypt(buffer);
					this.buffer.put(buffer);
					receiveProvidePadding();
					break;
				case receiveConfirm:
					this.buffer.put(buffer);
					receiveConfirm();
					break;
				case receiveConfirmPadding:
					this.cipher.decrypt(buffer);
					this.buffer.put(buffer);
					receiveConfirmPadding();
				default:
					LOGGER.warn("不支持的加密握手步骤：{}", this.step);
					break;
				}
			}
		} catch (NetException e) {
			LOGGER.debug("加密握手异常，使用明文。");
			this.plaintext();
			throw e;
		} catch (Exception e) {
			LOGGER.debug("加密握手异常，使用明文。");
			this.plaintext();
			throw new NetException("加密握手失败", e);
		}
	}
	
	/**
	 * 设置明文
	 */
	public void plaintext() {
		this.over(true, false);
	}
	
	/**
	 * 加密：不改变buffer读取和写入状态
	 */
	public void encrypt(ByteBuffer buffer) {
		if(this.crypt) {
			this.cipher.encrypt(buffer);
		}
	}
	
	/**
	 * 解密：不改变buffer读取和写入状态
	 */
	public void decrypt(ByteBuffer buffer) {
		if(this.crypt) {
			this.cipher.decrypt(buffer);
		}
	}
	
	/**
	 * 握手等待锁
	 */
	public void handshakeLock() {
		if(!this.over) {
			synchronized (this.handshakeLock) {
				if(!this.over) {
					ThreadUtils.wait(this.handshakeLock, Duration.ofSeconds(HANDSHAKE_LOCK_TIMEOUT));
				}
			}
		}
		if(!this.over) {
			LOGGER.debug("加密握手失败，使用明文。");
			this.plaintext();
		}
	}
	
	/**
	 * 唤醒握手等待
	 */
	private void handshakeUnlock() {
		synchronized (this.handshakeLock) {
			this.handshakeLock.notifyAll();
		}
	}

	/**
	 * <p>发送公钥</p>
	 * <pre>
	 * A->B: Diffie Hellman Ya, PadA
	 * B->A: Diffie Hellman Yb, PadB
	 * </pre>
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

	private static final int PUBLIC_KEY_MIN_LENGTH = CryptConfig.PUBLIC_KEY_LENGTH;
	private static final int PUBLIC_KEY_MAX_LENGTH = PUBLIC_KEY_MIN_LENGTH + CryptConfig.PADDING_MAX_LENGTH;
	
	/**
	 * 接收公钥
	 */
	private void receivePublicKey() throws NetException {
		LOGGER.debug("加密握手，接收公钥，步骤：{}", this.step);
		if(this.buffer.position() < PUBLIC_KEY_MIN_LENGTH) {
			return;
		}
		if(this.buffer.position() > PUBLIC_KEY_MAX_LENGTH) {
			throw new NetException("加密握手失败（长度错误）");
		}
//		Diffie Hellman Ya, PadA
//		Diffie Hellman Yb, PadB
		this.buffer.flip();
		final BigInteger publicKey = NumberUtils.decodeUnsigned(this.buffer, CryptConfig.PUBLIC_KEY_LENGTH);
		this.buffer.compact();
		this.dhSecret = MSEKeyPairBuilder.buildDHSecret(publicKey, this.keyPair.getPrivate());
		if(this.step == Step.receivePublicKey) {
			sendPublicKey();
			this.step = Step.receiveProvide;
		} else if(this.step == Step.sendPublicKey) {
			sendProvide();
			this.step = Step.receiveConfirm;
		}
	}
	
	/**
	 * <p>发送加密协议协商</p>
	 * <pre>
	 * HASH('req1', S),
	 * HASH('req2', SKEY) xor HASH('req3', S),
	 * ENCRYPT(VC, crypto_provide, len(PadC), PadC, len(IA)),
	 * ENCRYPT(IA)
	 * </pre>
	 */
	private void sendProvide() throws NetException {
		LOGGER.debug("加密握手，发送加密协议协商，步骤：{}", this.step);
		final TorrentSession torrentSession = this.peerSubMessageHandler.torrentSession();
		if(torrentSession == null) {
			throw new NetException("加密握手失败（种子不存在）");
		}
		final byte[] dhSecretBytes = NumberUtils.encodeUnsigned(this.dhSecret, CryptConfig.PUBLIC_KEY_LENGTH);
		final InfoHash infoHash = torrentSession.infoHash();
		this.cipher = MSECipher.newInitiator(dhSecretBytes, infoHash);
		this.cipherTmp = MSECipher.newInitiator(dhSecretBytes, infoHash);
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
//		ENCRYPT(IA)
//		没有IA数据
		this.cipher.encrypt(buffer);
		this.peerSubMessageHandler.send(buffer);
	}

	private static final int PROVIDE_MIN_LENGTH = 20 + 20 + 8 + 4 + 2 + 0 + 2 + 0;
	private static final int PROVIDE_MAX_LENGTH = PROVIDE_MIN_LENGTH + CryptConfig.PADDING_MAX_LENGTH;
	
	/**
	 * 接收加密协议协商
	 */
	private void receiveProvide() throws NetException {
		LOGGER.debug("加密握手，接收加密协议协商，步骤：{}", this.step);
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
			throw new NetException("加密握手失败（长度错误）");
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
			final byte[] req2 = digest.digest();
			if (ArrayUtils.equals(ArrayUtils.xor(req2, req3), req2x3)) {
				infoHash = tmp;
				break;
			}
		}
		if(infoHash == null) {
			throw new NetException("加密握手失败（种子不存在）");
		}
		this.cipher = MSECipher.newReceiver(dhSecretBytes, infoHash);
//		ENCRYPT(VC, crypto_provide, len(PadC), PadC, len(IA))
//		ENCRYPT(IA)
		this.buffer.compact(); // 删除已经读取过不需要解密的数据，留下被加密的数据。
		this.buffer.flip();
		this.cipher.decrypt(this.buffer);
		final byte[] vc = new byte[CryptConfig.VC_LENGTH];
		this.buffer.get(vc);
		final int provide = this.buffer.getInt(); // 协议选择
		LOGGER.debug("接收加密协议协商选择：{}", provide);
		this.strategy = selectStrategy(provide); // 选择协议
		this.step = Step.receiveProvidePadding;
		this.msePaddingSync = MSEPaddingSync.newInstance(2);
		this.receiveProvidePadding();
	}
	
	/**
	 * 接收加密协议协商Padding
	 */
	private void receiveProvidePadding() {
		LOGGER.debug("加密握手，接收加密协议协商Padding，步骤：{}", this.step);
		final boolean ok = this.msePaddingSync.sync(this.buffer);
		if(ok) {
			if(LOGGER.isDebugEnabled()) {
				this.msePaddingSync.allPadding().forEach(bytes -> {
					LOGGER.debug("Padding：{}", StringUtils.hex(bytes));
				});
			}
			sendConfirm();
		}
	}
	
	/**
	 * <p>发送确认加密协议</p>
	 * <pre>
	 * ENCRYPT(VC, crypto_select, len(padD), padD),
	 * ENCRYPT2(Payload Stream)
	 * </pre>
	 */
	private void sendConfirm() {
		LOGGER.debug("加密握手，发送确认加密协议，步骤：{}", this.step);
		final byte[] padding = buildZeroPadding(CryptConfig.PADDING_MAX_LENGTH);
		final int paddingLength = padding.length;
		final ByteBuffer buffer = ByteBuffer.allocate(14 + paddingLength); // 8 + 4 + 2 + Padding
		buffer.put(CryptConfig.VC);
		buffer.putInt(this.strategy.provide());
		buffer.putShort((short) paddingLength);
		buffer.put(padding);
		this.cipher.encrypt(buffer);
		this.peerSubMessageHandler.send(buffer);
		LOGGER.debug("发送确认加密协议：{}", this.strategy);
		this.over(true, this.strategy.crypt());
	}
	
	private static final int CONFIRM_MIN_LENGTH = 8 + 4 + 2 + 0;
	private static final int CONFIRM_MAX_LENGTH = CONFIRM_MIN_LENGTH + CryptConfig.PADDING_MAX_LENGTH;
	
	/**
	 * 接收确认加密协议
	 */
	private void receiveConfirm() throws NetException {
		LOGGER.debug("加密握手，接收确认加密协议，步骤：{}", this.step);
		final byte[] vcMatch = this.cipherTmp.decrypt(CryptConfig.VC);
		if(!match(vcMatch)) {
			return;
		}
		if(this.buffer.position() < CONFIRM_MIN_LENGTH) {
			return;
		}
		if(this.buffer.position() > CONFIRM_MAX_LENGTH) {
			throw new NetException("加密握手失败（长度错误）");
		}
//		ENCRYPT(VC, crypto_select, len(padD), padD)
		this.buffer.flip();
		this.cipher.decrypt(this.buffer);
		final byte[] vc = new byte[CryptConfig.VC_LENGTH];
		this.buffer.get(vc);
		final int provide = this.buffer.getInt(); // 协议选择
		LOGGER.debug("接收确认加密协议选择：{}", provide);
		this.strategy = selectStrategy(provide); // 选择协议
		LOGGER.debug("接收确认加密协议：{}", this.strategy);
		this.step = Step.receiveConfirmPadding;
		this.msePaddingSync = MSEPaddingSync.newInstance(1);
		this.receiveConfirmPadding();
	}
	
	/**
	 * 接收确认加密协议Padding
	 */
	private void receiveConfirmPadding() {
		LOGGER.debug("加密握手，接收确认加密协议Padding，步骤：{}", this.step);
		final boolean ok = this.msePaddingSync.sync(this.buffer);
		if(ok) {
			if(LOGGER.isDebugEnabled()) {
				this.msePaddingSync.allPadding().forEach(bytes -> {
					LOGGER.debug("Padding：{}", StringUtils.hex(bytes));
				});
			}
			this.over(true, this.strategy.crypt());
		}
	}
	
	/**
	 * <p>选择策略</p>
	 * <p>协商：根据接入客户端和本地综合选择出使用加密或者明文</p>
	 * <p>确认：加密或者明文</p>
	 */
	private CryptConfig.Strategy selectStrategy(int provide) throws NetException {
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
			throw new NetException("加密握手失败（加密协议不支持）：" + provide);
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
			padding[index] = (byte) random.nextInt(SystemConfig.UNSIGNED_BYTE_MAX);
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
	 * 判断是否是Peer握手消息，如果是Peer握手消息直接使用明文。
	 */
	private boolean checkPeerHandshake(ByteBuffer buffer) throws NetException {
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
	 * 设置完成
	 * 
	 * @param over 是否完成
	 * @param crypt 是否加密
	 */
	private void over(boolean over, boolean crypt) {
		this.over = over;
		this.crypt = crypt;
		this.buffer = null;
		this.keyPair = null;
		this.strategy = null;
		this.dhSecret = null;
		this.cipherTmp = null;
		this.msePaddingSync = null;
		this.handshakeUnlock();
	}

}
