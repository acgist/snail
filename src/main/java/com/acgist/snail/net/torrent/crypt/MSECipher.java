package com.acgist.snail.net.torrent.crypt;

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

import com.acgist.snail.pojo.bean.InfoHash;
import com.acgist.snail.system.exception.ArgumentException;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.DigestUtils;

/**
 * <p>MSE加密套件（ARC4）</p>
 * <p>协议链接：https://baike.baidu.com/item/RC4/3454548</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class MSECipher {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MSECipher.class);

	/**
	 * <p>加密算法名称</p>
	 */
	private static final String ARC4_ALGO = "ARCFOUR";
	/**
	 * <p>加密算法</p>
	 */
	private static final String ARC4_ALGO_TRANSFORMATION = ARC4_ALGO + "/ECB/NoPadding";

	/**
	 * <p>加密</p>
	 */
	private final Cipher encryptCipher;
	/**
	 * <p>解密</p>
	 */
	private final Cipher decryptCipher;
	
	/**
	 * <p>加密套件</p>
	 * 
	 * @param S DH Secret
	 * @param infoHash InfoHash
	 * @param sender 是否是请求客户端
	 */
	private MSECipher(byte[] S, InfoHash infoHash, boolean sender) {
		final Key sendKey = buildSendKey(S, infoHash.infoHash());
		final Key recvKey = buildRecvKey(S, infoHash.infoHash());
		final Key encryptKey = sender ? sendKey : recvKey;
		final Key decryptKey = sender ? recvKey : sendKey;
		this.decryptCipher = buildCipher(Cipher.DECRYPT_MODE, ARC4_ALGO_TRANSFORMATION, decryptKey);
		this.encryptCipher = buildCipher(Cipher.ENCRYPT_MODE, ARC4_ALGO_TRANSFORMATION, encryptKey);
	}
	
	/**
	 * <p>请求客户端</p>
	 * 
	 * @param S DH Secret
	 * @param infoHash InfoHash
	 */
	public static final MSECipher newSender(byte[] S, InfoHash infoHash) {
		return new MSECipher(S, infoHash, true);
	}
	
	/**
	 * <p>接入客户端</p>
	 * 
	 * @param S DH Secret
	 * @param infoHash InfoHash
	 */
	public static final MSECipher newRecver(byte[] S, InfoHash infoHash) {
		return new MSECipher(S, infoHash, false);
	}

	/**
	 * <p>加密</p>
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
	 * <p>加密</p>
	 */
	public byte[] encrypt(byte[] bytes) throws NetException {
		try {
			return this.getEncryptCipher().doFinal(bytes);
		} catch (Exception e) {
			throw new NetException("加密异常", e);
		}
	}
	
	/**
	 * <p>解密</p>
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
	 * <p>解密</p>
	 */
	public byte[] decrypt(byte[] bytes) throws NetException {
		try {
			return this.getDecryptCipher().doFinal(bytes);
		} catch (Exception e) {
			throw new NetException("解密异常", e);
		}
	}
	
	/**
	 * <p>加密Cipher</p>
	 */
	public Cipher getEncryptCipher() {
		return this.encryptCipher;
	}

	/**
	 * <p>解密Cipher</p>
	 */
	public Cipher getDecryptCipher() {
		return this.decryptCipher;
	}

	/**
	 * <p>创建请求客户端加密Key</p>
	 */
	private Key buildSendKey(byte[] S, byte[] SKEY) {
		return buildKey("keyA", S, SKEY);
	}

	/**
	 * <p>创建接入客户端加密Key</p>
	 */
	private Key buildRecvKey(byte[] S, byte[] SKEY) {
		return buildKey("keyB", S, SKEY);
	}

	/**
	 * <p>创建Key</p>
	 */
	private Key buildKey(String s, byte[] S, byte[] SKEY) {
		final MessageDigest digest = DigestUtils.sha1();
		digest.update(s.getBytes());
		digest.update(S);
		digest.update(SKEY);
		return new SecretKeySpec(digest.digest(), ARC4_ALGO);
	}

	/**
	 * <p>创建Cipher</p>
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
