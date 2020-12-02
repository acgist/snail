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

import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.pojo.bean.InfoHash;
import com.acgist.snail.utils.DigestUtils;

/**
 * <p>MSE加解密套件（ARC4）</p>
 * <p>协议链接：https://baike.baidu.com/item/RC4/3454548</p>
 * 
 * @author acgist
 */
public final class MSECipher {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MSECipher.class);

	/**
	 * <p>加密算法名称：{@value}</p>
	 */
	private static final String ARC4_ALGO = "ARCFOUR";
	/**
	 * <p>加密算法：{@value}</p>
	 * <p>NoPadding：没有填充</p>
	 * <p>ZeroPadding：填充零</p>
	 * <p>PKCS5Padding：块大小固定8的PKCS7Padding</p>
	 * <p>PKCS7Padding：填充块大小对齐</p>
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
	 * 
	 * @throws InvalidKeyException 密钥异常
	 * @throws NoSuchPaddingException 填充异常
	 * @throws NoSuchAlgorithmException 算法异常
	 */
	private MSECipher(Key encryptKey, Key decryptKey) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
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
	 * 
	 * @throws NetException 网络异常
	 */
	public static final MSECipher newSender(byte[] S, InfoHash infoHash) throws NetException {
		final Key sendKey = buildSendKey(S, infoHash.infoHash());
		final Key recvKey = buildRecvKey(S, infoHash.infoHash());
		try {
			return new MSECipher(sendKey, recvKey);
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
			throw new NetException("创建加密套件失败", e);
		}
	}
	
	/**
	 * <p>创建接入客户端加解密套件</p>
	 * 
	 * @param S DH Secret
	 * @param infoHash InfoHash
	 * 
	 * @return 加解密套件
	 * 
	 * @throws NetException 网络异常
	 */
	public static final MSECipher newRecver(byte[] S, InfoHash infoHash) throws NetException {
		final Key sendKey = buildSendKey(S, infoHash.infoHash());
		final Key recvKey = buildRecvKey(S, infoHash.infoHash());
		try {
			return new MSECipher(recvKey, sendKey);
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
			throw new NetException("创建加密套件失败", e);
		}
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
	 * @param s keyA | keyB
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
	 * 
	 * @throws InvalidKeyException 密钥异常
	 * @throws NoSuchPaddingException 填充异常
	 * @throws NoSuchAlgorithmException 算法异常
	 */
	private Cipher buildCipher(int mode, String transformation, Key key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
		final Cipher cipher = Cipher.getInstance(transformation);
		cipher.init(mode, key);
		cipher.update(new byte[1024]); // 丢弃1024字节
		return cipher;
	}

}
