package com.acgist.snail.net.torrent.peer.bootstrap.crypt;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.system.exception.ArgumentException;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.DigestUtils;

/**
 * <p>MSE加密套件（ARC4）</p>
 * <p>参考链接：https://baike.baidu.com/item/RC4/3454548</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class MSECipher {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MSECipher.class);

	private static final String ARC4_ALGO = "ARCFOUR";
	private static final String ARC4_ALGO_TRANSFORMATION = ARC4_ALGO + "/ECB/NoPadding";

	/**
	 * 加密
	 */
	private final Cipher encryptCipher;
	/**
	 * 解密
	 */
	private final Cipher decryptCipher;
	
	private MSECipher(byte[] S, InfoHash infoHash, boolean initiator) {
		final Key initiatorKey = buildInitiatorKey(S, infoHash.infoHash());
		final Key receiverKey = buildReceiverKey(S, infoHash.infoHash());
		final Key encryptKey = initiator ? initiatorKey : receiverKey;
		final Key decryptKey = initiator ? receiverKey : initiatorKey;
		this.decryptCipher = buildCipher(Cipher.DECRYPT_MODE, ARC4_ALGO_TRANSFORMATION, decryptKey);
		this.encryptCipher = buildCipher(Cipher.ENCRYPT_MODE, ARC4_ALGO_TRANSFORMATION, encryptKey);
	}
	
	/**
	 * 请求客户端
	 * 
	 * @param S DH Secret
	 * @param infoHash InfoHash
	 */
	public static final MSECipher newInitiator(byte[] S, InfoHash infoHash) {
		return new MSECipher(S, infoHash, true);
	}
	
	/**
	 * 连入客户端
	 * 
	 * @param S DH Secret
	 * @param infoHash InfoHash
	 */
	public static final MSECipher newReceiver(byte[] S, InfoHash infoHash) {
		return new MSECipher(S, infoHash, false);
	}

	/**
	 * 加密
	 */
	public void encrypt(ByteBuffer buffer) {
		synchronized (this) {
			try {
				boolean flip = true; // 标记状态
				if(buffer.position() != 0) {
					flip = false;
					buffer.flip();
				}
				final byte[] value = new byte[buffer.remaining()];
				buffer.get(value);
				final byte[] eValue = this.getEncryptCipher().update(value);
				buffer.clear().put(eValue);
				if(flip) {
					buffer.flip();
				}
			} catch (Exception e) {
				LOGGER.error("加密异常", e);
			}
		}
	}
	
	/**
	 * 加密
	 */
	public byte[] encrypt(byte[] bytes) throws NetException {
		try {
			return this.getEncryptCipher().doFinal(bytes);
		} catch (Exception e) {
			throw new NetException("加密异常", e);
		}
	}
	
	/**
	 * 解密
	 */
	public void decrypt(ByteBuffer buffer) {
		synchronized (this) {
			try {
				boolean flip = true; // 标记状态
				if(buffer.position() != 0) {
					flip = false;
					buffer.flip();
				}
				final byte[] value = new byte[buffer.remaining()];
				buffer.get(value);
				final byte[] dValue = this.getDecryptCipher().update(value);
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
	 * 解密
	 */
	public byte[] decrypt(byte[] bytes) throws NetException {
		try {
			return this.getDecryptCipher().doFinal(bytes);
		} catch (Exception e) {
			throw new NetException("解密异常", e);
		}
	}
	
	/**
	 * 加密Cipher
	 */
	public Cipher getEncryptCipher() {
		return this.encryptCipher;
	}

	/**
	 * 解密Cipher
	 */
	public Cipher getDecryptCipher() {
		return this.decryptCipher;
	}

	/**
	 * 创建请求客户端加密Key
	 */
	private Key buildInitiatorKey(byte[] S, byte[] SKEY) {
		return buildKey("keyA", S, SKEY);
	}

	/**
	 * 创建连入客户端加密Key
	 */
	private Key buildReceiverKey(byte[] S, byte[] SKEY) {
		return buildKey("keyB", S, SKEY);
	}

	/**
	 * 创建Key
	 */
	private Key buildKey(String s, byte[] S, byte[] SKEY) {
		final MessageDigest digest = DigestUtils.sha1();
		digest.update(s.getBytes());
		digest.update(S);
		digest.update(SKEY);
		return new SecretKeySpec(digest.digest(), ARC4_ALGO);
	}

	/**
	 * 创建Cipher
	 */
	private Cipher buildCipher(int mode, String transformation, Key key) {
		try {
			final Cipher cipher = Cipher.getInstance(transformation);
			cipher.init(mode, key);
			cipher.update(new byte[1024]); // 丢弃1024字符
			return cipher;
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
			throw new ArgumentException("不支持的加密算法：" + transformation, e);
		}
	}

}
