package com.acgist.snail.net.torrent.crypt;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.CryptConfig;
import com.acgist.snail.config.CryptConfig.CryptAlgo;
import com.acgist.snail.config.CryptConfig.Strategy;
import com.acgist.snail.config.PeerConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.context.exception.PacketSizeException;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.bootstrap.PeerUnpackMessageCodec;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerSubMessageHandler;
import com.acgist.snail.pojo.bean.InfoHash;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.DigestUtils;
import com.acgist.snail.utils.NumberUtils;
import com.acgist.snail.utils.StringUtils;

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
 */
public final class MSECryptHandshakeHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MSECryptHandshakeHandler.class);

	/**
	 * <p>缓冲区大小：{@value}</p>
	 */
	private static final int BUFFER_LENGTH = 4 * SystemConfig.ONE_KB;
	/**
	 * <p>加密握手超时时间：{@value}</p>
	 * <p>不能超过{@link PeerSubMessageHandler#HANDSHAKE_TIMEOUT}</p>
	 */
	private static final int HANDSHAKE_TIMEOUT = PeerSubMessageHandler.HANDSHAKE_TIMEOUT * SystemConfig.ONE_SECOND_MILLIS;
	
	/**
	 * <p>加密握手步骤</p>
	 * 
	 * @author acgist
	 */
	public enum Step {
		
		/** 发送公钥 */
		SEND_PUBLIC_KEY,
		/** 接收公钥 */
		RECEIVE_PUBLIC_KEY,
		/** 发送加密协议协商 */
		SEND_PROVIDE,
		/** 接收加密协议协商 */
		RECEIVE_PROVIDE,
		/** 接收加密协议协商Padding */
		RECEIVE_PROVIDE_PADDING,
		/** 发送确认加密协议 */
		SEND_CONFIRM,
		/** 接收确认加密协议 */
		RECEIVE_CONFIRM,
		/** 接收确认加密协议Padding */
		RECEIVE_CONFIRM_PADDING;
		
	}
	
	/**
	 * <p>当前步骤</p>
	 * <p>默认：接收公钥</p>
	 * 
	 * @see Step
	 */
	private Step step = Step.RECEIVE_PUBLIC_KEY;
	/**
	 * <p>是否加密</p>
	 * <p>默认：明文</p>
	 */
	private volatile boolean crypt = false;
	/**
	 * <p>握手是否完成</p>
	 */
	private volatile boolean complete = false;
	/**
	 * <p>加密握手锁</p>
	 */
	private final Object handshakeLock = new Object();
	/**
	 * <p>加密套件</p>
	 */
	private MSECipher cipher;
	/**
	 * <p>临时加密套件</p>
	 * <p>VC数据查找时使用，加密握手完成后释放。</p>
	 */
	private MSECipher cipherVC;
	/**
	 * <p>密钥对</p>
	 */
	private KeyPair keyPair;
	/**
	 * <p>加密策略</p>
	 */
	private Strategy strategy;
	/**
	 * <p>数据缓冲</p>
	 */
	private ByteBuffer buffer;
	/**
	 * <p>S：DH Secret</p>
	 */
	private BigInteger dhSecret;
	/**
	 * <p>Padding数据同步工具</p>
	 */
	private MSEPaddingSync msePaddingSync;
	/**
	 * <p>Peer消息代理</p>
	 */
	private final PeerSubMessageHandler peerSubMessageHandler;
	/**
	 * <p>Peer消息处理器</p>
	 */
	private final PeerUnpackMessageCodec peerUnpackMessageCodec;

	/**
	 * @param peerUnpackMessageCodec Peer消息处理器
	 * @param peerSubMessageHandler Peer消息代理
	 */
	private MSECryptHandshakeHandler(PeerUnpackMessageCodec peerUnpackMessageCodec, PeerSubMessageHandler peerSubMessageHandler) {
		final MSEKeyPairBuilder mseKeyPairBuilder = MSEKeyPairBuilder.newInstance();
		this.buffer = ByteBuffer.allocate(BUFFER_LENGTH);
		this.keyPair = mseKeyPairBuilder.buildKeyPair();
		this.peerSubMessageHandler = peerSubMessageHandler;
		this.peerUnpackMessageCodec = peerUnpackMessageCodec;
	}

	/**
	 * <p>创建加密代理</p>
	 * 
	 * @param peerUnpackMessageCodec Peer消息处理器
	 * @param peerSubMessageHandler Peer消息代理
	 * 
	 * @return 加密代理
	 */
	public static final MSECryptHandshakeHandler newInstance(PeerUnpackMessageCodec peerUnpackMessageCodec, PeerSubMessageHandler peerSubMessageHandler) {
		return new MSECryptHandshakeHandler(peerUnpackMessageCodec, peerSubMessageHandler);
	}
	
	/**
	 * <p>设置明文</p>
	 */
	public void plaintext() {
		this.complete(true, false);
	}
	
	/**
	 * <p>判断握手是否完成</p>
	 * 
	 * @return true-完成；false-没有完成；
	 */
	public boolean complete() {
		return this.complete;
	}
	
	/**
	 * <p>发送握手消息</p>
	 */
	public void handshake() {
		this.step = Step.SEND_PUBLIC_KEY;
		this.sendPublicKey();
	}

	/**
	 * <p>处理握手消息</p>
	 * 
	 * @param buffer 握手消息
	 * 
	 * @throws NetException 网络异常
	 */
	public void handshake(ByteBuffer buffer) throws NetException {
		// buffer.flip(); // 此处不需要调用此方法，解密时已经调用。
		try {
			// 判断Peer握手消息（明文）
			if(this.checkPeerHandshake(buffer)) {
				LOGGER.debug("加密握手（跳过）：收到Peer握手消息");
				return;
			}
			synchronized (this.buffer) {
				switch (this.step) {
				case SEND_PUBLIC_KEY:
				case RECEIVE_PUBLIC_KEY:
					this.buffer.put(buffer);
					this.receivePublicKey();
					break;
				case RECEIVE_PROVIDE:
					this.buffer.put(buffer);
					this.receiveProvide();
					break;
				case RECEIVE_PROVIDE_PADDING:
					this.cipher.decrypt(buffer);
					this.buffer.put(buffer);
					this.receiveProvidePadding();
					break;
				case RECEIVE_CONFIRM:
					this.buffer.put(buffer);
					this.receiveConfirm();
					break;
				case RECEIVE_CONFIRM_PADDING:
					this.cipher.decrypt(buffer);
					this.buffer.put(buffer);
					this.receiveConfirmPadding();
					break;
				default:
					LOGGER.warn("加密握手失败（步骤不支持）：{}", this.step);
					break;
				}
			}
		} catch (NetException e) {
			LOGGER.debug("加密握手失败：使用明文");
			this.plaintext();
			throw e;
		} catch (Exception e) {
			LOGGER.debug("加密握手失败：使用明文");
			this.plaintext();
			throw new NetException("加密握手失败", e);
		}
	}
	
	/**
	 * <p>判断是否需要加密</p>
	 * 
	 * @return true-加密；false-明文；
	 */
	public boolean needEncrypt() {
		return this.peerSubMessageHandler.needEncrypt();
	}
	
	/**
	 * <p>数据加密</p>
	 * <p>不改变buffer读取和写入状态</p>
	 * 
	 * @param buffer 数据
	 */
	public void encrypt(ByteBuffer buffer) {
		if(this.crypt) {
			this.cipher.encrypt(buffer);
		}
	}
	
	/**
	 * <p>数据解密</p>
	 * <p>不改变buffer读取和写入状态</p>
	 * 
	 * @param buffer 数据
	 */
	public void decrypt(ByteBuffer buffer) {
		if(this.crypt) {
			this.cipher.decrypt(buffer);
		}
	}
	
	/**
	 * <p>添加加密握手锁</p>
	 */
	public void lockHandshake() {
		if(!this.complete) {
			// 加锁
			synchronized (this.handshakeLock) {
				if(!this.complete) {
					try {
						this.handshakeLock.wait(HANDSHAKE_TIMEOUT);
					} catch (InterruptedException e) {
						LOGGER.debug("线程等待异常", e);
						Thread.currentThread().interrupt();
					}
				}
			}
		}
		// 加密没有完成设置明文
		if(!this.complete) {
			LOGGER.debug("加密握手失败：使用明文");
			this.plaintext();
		}
	}
	
	/**
	 * <p>释放加密握手锁</p>
	 */
	private void unlockHandshake() {
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
		LOGGER.debug("加密握手（发送公钥）步骤：{}", this.step);
		final byte[] publicKey = this.keyPair.getPublic().getEncoded();
		final byte[] padding = this.buildPadding(CryptConfig.PADDING_MAX_LENGTH);
		final ByteBuffer buffer = ByteBuffer.allocate(publicKey.length + padding.length);
		buffer.put(publicKey);
		buffer.put(padding);
		this.peerSubMessageHandler.send(buffer);
	}

	/**
	 * <p>公钥消息最小长度：{@value}</p>
	 */
	private static final int PUBLIC_KEY_MIN_LENGTH = CryptConfig.PUBLIC_KEY_LENGTH;
	/**
	 * <p>公钥消息最大长度：{@value}</p>
	 */
	private static final int PUBLIC_KEY_MAX_LENGTH = PUBLIC_KEY_MIN_LENGTH + CryptConfig.PADDING_MAX_LENGTH;
	
	/**
	 * <p>接收公钥</p>
	 * 
	 * @throws NetException 网络异常
	 */
	private void receivePublicKey() throws NetException {
		LOGGER.debug("加密握手（接收公钥）步骤：{}", this.step);
		if(this.buffer.position() < PUBLIC_KEY_MIN_LENGTH) {
			return;
		}
		if(this.buffer.position() > PUBLIC_KEY_MAX_LENGTH) {
			throw new NetException("加密握手失败（长度错误）");
		}
//		Diffie Hellman Ya, PadA
//		Diffie Hellman Yb, PadB
		this.buffer.flip();
		final BigInteger publicKey = NumberUtils.decodeBigInteger(this.buffer, CryptConfig.PUBLIC_KEY_LENGTH);
		this.buffer.compact();
		try {
			this.dhSecret = MSEKeyPairBuilder.buildDHSecret(publicKey, this.keyPair.getPrivate());
		} catch (InvalidKeyException e) {
			throw new NetException("获取密钥失败", e);
		}
		if(this.step == Step.RECEIVE_PUBLIC_KEY) {
			this.sendPublicKey();
			this.step = Step.RECEIVE_PROVIDE;
		} else if(this.step == Step.SEND_PUBLIC_KEY) {
			this.sendProvide();
			this.step = Step.RECEIVE_CONFIRM;
		} else {
			LOGGER.warn("加密握手失败（接收公钥未知步骤）：{}", this.step);
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
		LOGGER.debug("加密握手（发送加密协议协商）步骤：{}", this.step);
		final TorrentSession torrentSession = this.peerSubMessageHandler.torrentSession();
		if(torrentSession == null) {
			throw new NetException("加密握手失败（种子信息不存在）");
		}
		final byte[] dhSecretBytes = NumberUtils.encodeBigInteger(this.dhSecret, CryptConfig.PUBLIC_KEY_LENGTH);
		final InfoHash infoHash = torrentSession.infoHash();
		this.cipher = MSECipher.newSender(dhSecretBytes, infoHash);
		this.cipherVC = MSECipher.newSender(dhSecretBytes, infoHash);
		ByteBuffer buffer = ByteBuffer.allocate(40); // 20 + 20
		final MessageDigest digest = DigestUtils.sha1();
//		HASH('req1', S)
		digest.update("req1".getBytes());
		digest.update(dhSecretBytes);
		buffer.put(digest.digest());
//		HASH('req2', SKEY) xor HASH('req3', S)
		digest.reset();
		digest.update("req2".getBytes());
		digest.update(infoHash.infoHash());
		final byte[] req2 = digest.digest();
		digest.reset();
		digest.update("req3".getBytes());
		digest.update(dhSecretBytes);
		final byte[] req3 = digest.digest();
		buffer.put(ArrayUtils.xor(req2, req3));
		this.peerSubMessageHandler.send(buffer);
//		ENCRYPT(VC, crypto_provide, len(PadC), PadC, len(IA))
		final byte[] padding = this.buildZeroPadding(CryptConfig.PADDING_MAX_LENGTH);
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

	/**
	 * <p>加密协商消息最小长度：{@value}</p>
	 */
	private static final int PROVIDE_MIN_LENGTH = 20 + 20 + 8 + 4 + 2 + 0 + 2 + 0;
	/**
	 * <p>加密协商消息最大长度：{@value}</p>
	 */
	private static final int PROVIDE_MAX_LENGTH = PROVIDE_MIN_LENGTH + CryptConfig.PADDING_MAX_LENGTH;
	
	/**
	 * <p>接收加密协议协商</p>
	 * 
	 * @throws NetException 网络异常
	 */
	private void receiveProvide() throws NetException {
		LOGGER.debug("加密握手（接收加密协议协商）步骤：{}", this.step);
		final byte[] dhSecretBytes = NumberUtils.encodeBigInteger(this.dhSecret, CryptConfig.PUBLIC_KEY_LENGTH);
		final MessageDigest digest = DigestUtils.sha1();
//		HASH('req1', S)
		digest.update("req1".getBytes());
		digest.update(dhSecretBytes);
		final byte[] req1 = digest.digest();
		// 匹配数据
		if(!this.match(req1)) {
			return;
		}
		if(this.buffer.position() < PROVIDE_MIN_LENGTH) {
			return;
		}
		if(this.buffer.position() > PROVIDE_MAX_LENGTH) {
			throw new NetException("加密握手失败（长度错误）");
		}
		this.buffer.flip();
		final byte[] req1Peer = new byte[20];
		this.buffer.get(req1Peer);
		// 数据匹配成功不再验证req1
//		if(!ArrayUtils.equals(req1, req1Peer)) {
//			throw new NetException("加密握手失败（数据匹配错误）");
//		}
//		HASH('req2', SKEY) xor HASH('req3', S)
		final byte[] req2x3Peer = new byte[20];
		this.buffer.get(req2x3Peer);
		digest.reset();
		digest.update("req3".getBytes());
		digest.update(dhSecretBytes);
		final byte[] req3 = digest.digest();
		InfoHash infoHash = null;
		// 获取种子信息
		for (InfoHash tmp : TorrentManager.getInstance().allInfoHash()) {
			digest.reset();
			digest.update("req2".getBytes());
			digest.update(tmp.infoHash());
			final byte[] req2 = digest.digest();
			if (ArrayUtils.equals(ArrayUtils.xor(req2, req3), req2x3Peer)) {
				infoHash = tmp;
				break;
			}
		}
		if(infoHash == null) {
			throw new NetException("加密握手失败（种子信息不存在）");
		}
		this.cipher = MSECipher.newRecver(dhSecretBytes, infoHash);
//		ENCRYPT(VC, crypto_provide, len(PadC), PadC, len(IA))
//		ENCRYPT(IA)
		this.buffer.compact(); // 删除已经读取过不需要解密的数据，留下被加密的数据。
		this.buffer.flip();
		this.cipher.decrypt(this.buffer);
		final byte[] vc = new byte[CryptConfig.VC_LENGTH];
		this.buffer.get(vc);
		final int provide = this.buffer.getInt(); // 协商选择
		LOGGER.debug("加密握手（接收加密协议协商选择）：{}", provide);
		this.strategy = this.selectStrategy(provide); // 选择策略
		this.step = Step.RECEIVE_PROVIDE_PADDING;
		this.msePaddingSync = MSEPaddingSync.newInstance(2); // PadC IA
		this.receiveProvidePadding();
	}
	
	/**
	 * <p>接收加密协议协商Padding</p>
	 * 
	 * @throws PacketSizeException 数据包大小异常
	 */
	private void receiveProvidePadding() throws PacketSizeException {
		LOGGER.debug("加密握手（接收加密协议协商Padding）步骤：{}", this.step);
		final boolean success = this.msePaddingSync.sync(this.buffer);
		if(success) {
			if(LOGGER.isDebugEnabled()) {
				this.msePaddingSync.allPadding().forEach(bytes -> {
					LOGGER.debug("加密握手（接收加密协议协商Padding）：{}", StringUtils.hex(bytes));
				});
			}
			this.sendConfirm();
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
		LOGGER.debug("加密握手（发送确认加密协议）步骤：{}", this.step);
		final byte[] padding = this.buildZeroPadding(CryptConfig.PADDING_MAX_LENGTH);
		final int paddingLength = padding.length;
		final ByteBuffer buffer = ByteBuffer.allocate(14 + paddingLength); // 8 + 4 + 2 + Padding
		buffer.put(CryptConfig.VC);
		buffer.putInt(this.strategy.provide());
		buffer.putShort((short) paddingLength);
		buffer.put(padding);
		this.cipher.encrypt(buffer);
		this.peerSubMessageHandler.send(buffer);
		LOGGER.debug("加密握手（发送确认加密协议）：{}", this.strategy);
		this.complete(true, this.strategy.crypt());
	}
	
	/**
	 * <p>确认加密消息最小长度：{@value}</p>
	 */
	private static final int CONFIRM_MIN_LENGTH = 8 + 4 + 2 + 0;
	/**
	 * <p>确认加密消息最大长度：{@value}</p>
	 */
	private static final int CONFIRM_MAX_LENGTH = CONFIRM_MIN_LENGTH + CryptConfig.PADDING_MAX_LENGTH;
	
	/**
	 * <p>接收确认加密协议</p>
	 * 
	 * @throws NetException 网络异常
	 */
	private void receiveConfirm() throws NetException {
		LOGGER.debug("加密握手（接收确认加密协议）步骤：{}", this.step);
		final byte[] vcMatch = this.cipherVC.decrypt(CryptConfig.VC);
		if(!this.match(vcMatch)) {
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
		final int provide = this.buffer.getInt(); // 协商选择
		LOGGER.debug("加密握手（接收确认加密协商选择）：{}", provide);
		this.strategy = this.selectStrategy(provide); // 选择策略
		LOGGER.debug("加密握手（接收确认加密协议）：{}", this.strategy);
		this.step = Step.RECEIVE_CONFIRM_PADDING;
		this.msePaddingSync = MSEPaddingSync.newInstance(1);
		this.receiveConfirmPadding();
	}
	
	/**
	 * <p>接收确认加密协议Padding</p>
	 * 
	 * @throws PacketSizeException 网络包大小异常
	 */
	private void receiveConfirmPadding() throws PacketSizeException {
		LOGGER.debug("加密握手（接收确认加密协议Padding）步骤：{}", this.step);
		final boolean success = this.msePaddingSync.sync(this.buffer);
		if(success) {
			if(LOGGER.isDebugEnabled()) {
				this.msePaddingSync.allPadding().forEach(bytes -> {
					LOGGER.debug("加密握手（接收确认加密协议Padding）：{}", StringUtils.hex(bytes));
				});
			}
			this.complete(true, this.strategy.crypt());
		}
	}
	
	/**
	 * <p>选择加密策略</p>
	 * <p>协商：根据接入客户端和本地综合选择出使用加密或者明文</p>
	 * <p>确认：加密或者明文</p>
	 * 
	 * @param provide 加密协商
	 * 
	 * @return 加密策略
	 */
	private CryptConfig.Strategy selectStrategy(int provide) throws NetException {
		// 客户端是否支持明文
		final boolean plaintext = (provide & CryptAlgo.PLAINTEXT.provide()) == CryptAlgo.PLAINTEXT.provide();
		// 客户端是否支持加密
		final boolean crypt = (provide & CryptAlgo.ARC4.provide()) == CryptAlgo.ARC4.provide();
		Strategy selected = null;
		if (plaintext || crypt) {
			switch (CryptConfig.STRATEGY) { // 本地策略
			case PLAINTEXT:
				selected = plaintext ? Strategy.PLAINTEXT : null;
				break;
			case PREFER_PLAINTEXT:
				selected = plaintext ? Strategy.PLAINTEXT : Strategy.ENCRYPT;
				break;
			case PREFER_ENCRYPT:
				selected = crypt ? Strategy.ENCRYPT : Strategy.PLAINTEXT;
				break;
			case ENCRYPT:
				selected = crypt ? Strategy.ENCRYPT : null;
				break;
			default:
				selected = CryptConfig.STRATEGY.crypt() ? Strategy.ENCRYPT : Strategy.PLAINTEXT;
				break;
			}
		}
		if (selected == null) {
			throw new NetException("加密握手失败（加密协商不支持）：" + provide);
		}
		return selected;
	}
	
	/**
	 * <p>填充：随机值</p>
	 * 
	 * @param maxLength 填充长度
	 * 
	 * @return 填充数据
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
	 * <p>填充：{@code 0}</p>
	 * 
	 * @param maxLength 填充长度
	 * 
	 * @return 填充数据
	 */
	private byte[] buildZeroPadding(int maxLength) {
		final Random random = NumberUtils.random();
		return new byte[random.nextInt(maxLength + 1)];
	}
	
	/**
	 * <p>判断是否是Peer握手消息</p>
	 * <p>如果是Peer握手消息直接使用明文</p>
	 * 
	 * @param buffer 消息
	 * 
	 * @return true-Peer握手；false-加密握手；
	 * 
	 * @throws NetException 网络异常
	 */
	private boolean checkPeerHandshake(ByteBuffer buffer) throws NetException {
		final byte first = buffer.get();
		// 判断首个字符
		if(first == PeerConfig.PROTOCOL_NAME_LENGTH && buffer.remaining() >= PeerConfig.PROTOCOL_NAME_LENGTH) {
			// 判断协议名称
			final byte[] names = new byte[PeerConfig.PROTOCOL_NAME_LENGTH];
			buffer.get(names);
			if(ArrayUtils.equals(names, PeerConfig.PROTOCOL_NAME_BYTES)) {
				// 握手消息直接使用明文
				this.plaintext();
				buffer.position(0); // 重置长度
				this.peerUnpackMessageCodec.decode(buffer); // 处理消息
				return true;
			}
		}
		buffer.position(0); // 重置长度
		return false;
	}
	
	/**
	 * <p>判断数据是否匹配成功</p>
	 * 
	 * @param bytes 匹配数据
	 * 
	 * @return true-成功；false-失败；
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
	 * <p>设置握手完成</p>
	 * 
	 * @param complete 是否完成
	 * @param crypt 是否加密
	 */
	private void complete(boolean complete, boolean crypt) {
		this.complete = complete;
		this.crypt = crypt;
		this.buffer = null;
		this.keyPair = null;
		this.strategy = null;
		this.dhSecret = null;
		this.cipherVC = null;
		this.msePaddingSync = null;
		this.unlockHandshake();
	}

}
