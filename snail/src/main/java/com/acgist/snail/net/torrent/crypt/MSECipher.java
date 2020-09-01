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
 * <p>MSE加解密套件（ARC4）</p>
 * <p>协议链接：https://baike.baidu.com/item/RC4/3454548</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class MSECipher {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MSECipher.class);

	/**
	 * <p>加密算法名称：{@value}</p>
	 */
	private static final String ARC4_ALGO = "ARCFOUR";
	/**
	 * <p>加密算法：{@value}</p>
	 */
	private static final String ARC4_ALGO_TRANSFORMATION = ARC4_ALGO + "/ECB/NoPadding";

	/**
	 * <p>加密套件</p>
	 */
	private final Cipher encryptCipher;
	/**
	 * <p>解密套件</p>
	 */
	private final Cipher decryptCipher;
	
	/**
	 * <p>加解密套件</p>
	 * 
	 * @param encryptKey 加密Key
	 * @param decryptKey 解密Key
	 */
	private MSECipher(Key encryptKey, Key decryptKey) {
		this.encryptCipher = this.buildCipher(Cipher.ENCRYPT_MODE, ARC4_ALGO_TRANSFORMATION, encryptKey);
		this.decryptCipher = this.buildCipher(Cipher.DECRYPT_MODE, ARC4_ALGO_TRANSFORMATION, decryptKey);
	}
	
	/**
	 * <p>创建请求客户端加解密套件</p>
	 * 
	 * @param S DH Secret
	 * @param infoHash InfoHash
	 * 
	 * @return 加解密套件
	 */
	public static final MSECipher newSender(byte[] S, InfoHash infoHash) {
		final Key sendKey = buildSendKey(S, infoHash.infoHash());
		final Key recvKey = buildRecvKey(S, infoHash.infoHash());
		return new MSECipher(sendKey, recvKey);
	}
	
	/**
	 * <p>创建接入客户端加解密套件</p>
	 * 
	 * @param S DH Secret
	 * @param infoHash InfoHash
	 * 
	 * @return 加解密套件
	 */
	public static final MSECipher newRecver(byte[] S, InfoHash infoHash) {
		final Key sendKey = buildSendKey(S, infoHash.infoHash());
		final Key recvKey = buildRecvKey(S, infoHash.infoHash());
		return new MSECipher(recvKey, sendKey);
	}

	/**
	 * <p>数据加密</p>
	 * 
	 * @param buffer 数据
	 */
	public void encrypt(ByteBuffer buffer) {
		try {
			boolean flip = true; // 标记状态
			if(buffer.position() != 0) {
				flip = false;
				buffer.flip();
			}
			final byte[] value = new byte[buffer.remaining()];
			buffer.get(value);
			byte[] encryptValue;
			synchronized (this.encryptCipher) {
				encryptValue = this.encryptCipher.update(value);
			}
			buffer.clear().put(encryptValue);
			if(flip) {
				buffer.flip();
			}
		} catch (Exception e) {
			LOGGER.error("数据加密异常", e);
		}
	}
	
	/**
	 * <p>数据加密</p>
	 * 
	 * @param bytes 原始数据
	 * 
	 * @return 加密数据
	 * 
	 * @throws NetException 网络异常
	 */
	public byte[] encrypt(byte[] bytes) throws NetException {
		try {
			synchronized (this.encryptCipher) {
				return this.encryptCipher.doFinal(bytes);
			}
		} catch (Exception e) {
			throw new NetException("数据加密异常", e);
		}
	}
	
	/**
	 * <p>数据解密</p>
	 * 
	 * @param buffer 数据
	 */
	public void decrypt(ByteBuffer buffer) {
		try {
			boolean flip = true; // 标记状态
			if(buffer.position() != 0) {
				flip = false;
				buffer.flip();
			}
			final byte[] value = new byte[buffer.remaining()];
			buffer.get(value);
			byte[] decryptValue;
			synchronized (this.decryptCipher) {
				decryptValue = this.decryptCipher.update(value);
			}
			buffer.clear().put(decryptValue);
			if(flip) {
				buffer.flip();
			}
		} catch (Exception e) {
			LOGGER.error("数据解密异常", e);
		}
	}
	
	/**
	 * <p>数据解密</p>
	 * 
	 * @param bytes 加密数据
	 * 
	 * @return 原始数据
	 * 
	 * @throws NetException 网络异常
	 */
	public byte[] decrypt(byte[] bytes) throws NetException {
		try {
			synchronized (this.decryptCipher) {
				return this.decryptCipher.doFinal(bytes);
			}
		} catch (Exception e) {
			throw new NetException("数据解密异常", e);
		}
	}
	
	/**
	 * <p>创建请求客户端加密Key</p>
	 * 
	 * @param S DH Secret
	 * @param SKEY InfoHash
	 * 
	 * @return Key
	 */
	private static final Key buildSendKey(byte[] S, byte[] SKEY) {
		return buildKey("keyA", S, SKEY);
	}

	/**
	 * <p>创建接入客户端加密Key</p>
	 * 
	 * @param S DH Secret
	 * @param SKEY InfoHash
	 * 
	 * @return Key
	 */
	private static final Key buildRecvKey(byte[] S, byte[] SKEY) {
		return buildKey("keyB", S, SKEY);
	}

	/**
	 * <p>创建Key</p>
	 * 
	 * @param s {@code keyA} | {@code keyB}
	 * @param S DH Secret
	 * @param SKEY InfoHash
	 * 
	 * @return Key
	 */
	private static final Key buildKey(String s, byte[] S, byte[] SKEY) {
		final MessageDigest digest = DigestUtils.sha1();
		digest.update(s.getBytes());
		digest.update(S);
		digest.update(SKEY);
		return new SecretKeySpec(digest.digest(), ARC4_ALGO);
	}

	/**
	 * <p>创建Cipher</p>
	 * 
	 * @param mode 模式
	 * @param transformation 算法
	 * @param key Key
	 * 
	 * @return 加解密套件
	 */
	private Cipher buildCipher(int mode, String transformation, Key key) {
		try {
			final Cipher cipher = Cipher.getInstance(transformation);
			cipher.init(mode, key);
			cipher.update(new byte[1024]); // 丢弃1024字节
			return cipher;
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
			throw new ArgumentException("创建Cipher失败（不支持的算法）：" + transformation, e);
		}
	}

}
