package com.acgist.snail.net.torrent.codec;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import com.acgist.snail.net.NetException;
import com.acgist.snail.net.torrent.InfoHash;
import com.acgist.snail.utils.ByteUtils;
import com.acgist.snail.utils.DigestUtils;

/**
 * <p>MSE加解密套件（ARC4）</p>
 * <p>协议链接：https://baike.baidu.com/item/RC4/3454548</p>
 * 
 * @author acgist
 */
public final class MSECipher {
	
	/**
	 * <p>加密算法名称：{@value}</p>
	 */
	private static final String ARC4_ALGO = "ARCFOUR";
	/**
	 * <p>加密算法：{@value}</p>
	 * <p>NoPadding：没有填充</p>
	 * <p>ZeroPadding：填充零</p>
	 * <p>PKCS5Padding：填充对齐（块大小固定为8的PKCS7Padding）</p>
	 * <p>PKCS7Padding：填充对齐（块大小不固定）</p>
	 */
	private static final String ARC4_ALGO_TRANSFORMATION = ARC4_ALGO + "/ECB/NoPadding";
	/**
	 * <p>请求客户端Key：{@value}</p>
	 */
	private static final String KEY_SEND = "keyA";
	/**
	 * <p>接入客户端Key：{@value}</p>
	 */
	private static final String KEY_RECV = "keyB";

	/**
	 * <p>加密套件</p>
	 */
	private final Cipher encryptCipher;
	/**
	 * <p>解密套件</p>
	 */
	private final Cipher decryptCipher;
	
	/**
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
	 * <p>新建请求客户端加解密套件</p>
	 * 
	 * @param secret DH Secret
	 * @param infoHash InfoHash
	 * 
	 * @return MSE加解密套件
	 * 
	 * @throws NetException 网络异常
	 */
	public static final MSECipher newSender(byte[] secret, InfoHash infoHash) throws NetException {
		final Key sendKey = buildSendKey(secret, infoHash.infoHash());
		final Key recvKey = buildRecvKey(secret, infoHash.infoHash());
		try {
			return new MSECipher(sendKey, recvKey);
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
			throw new NetException("新建加密套件失败", e);
		}
	}
	
	/**
	 * <p>新建接入客户端加解密套件</p>
	 * 
	 * @param secret DH Secret
	 * @param infoHash InfoHash
	 * 
	 * @return MSE加解密套件
	 * 
	 * @throws NetException 网络异常
	 */
	public static final MSECipher newRecver(byte[] secret, InfoHash infoHash) throws NetException {
		final Key sendKey = buildSendKey(secret, infoHash.infoHash());
		final Key recvKey = buildRecvKey(secret, infoHash.infoHash());
		try {
			return new MSECipher(recvKey, sendKey);
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
			throw new NetException("新建加密套件失败", e);
		}
	}

	/**
	 * <p>数据加密</p>
	 * 
	 * @param buffer 数据
	 */
	public void encrypt(ByteBuffer buffer) {
		boolean flip = true;
		if(buffer.position() != 0) {
			flip = false;
			buffer.flip();
		}
		final byte[] value = ByteUtils.remainingToBytes(buffer);
		byte[] encryptValue;
		synchronized (this.encryptCipher) {
			encryptValue = this.encryptCipher.update(value);
		}
		buffer.clear().put(encryptValue);
		if(flip) {
			buffer.flip();
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
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			throw new NetException("数据加密异常", e);
		}
	}
	
	/**
	 * <p>数据解密</p>
	 * 
	 * @param buffer 数据
	 */
	public void decrypt(ByteBuffer buffer) {
		boolean flip = true;
		if(buffer.position() != 0) {
			flip = false;
			buffer.flip();
		}
		final byte[] value = ByteUtils.remainingToBytes(buffer);
		byte[] decryptValue;
		synchronized (this.decryptCipher) {
			decryptValue = this.decryptCipher.update(value);
		}
		buffer.clear().put(decryptValue);
		if(flip) {
			buffer.flip();
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
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			throw new NetException("数据解密异常", e);
		}
	}
	
	/**
	 * <p>新建请求客户端加密Key</p>
	 * 
	 * @param secret DH Secret
	 * @param skey InfoHash
	 * 
	 * @return Key
	 */
	private static final Key buildSendKey(byte[] secret, byte[] skey) {
		return buildKey(KEY_SEND, secret, skey);
	}

	/**
	 * <p>新建接入客户端加密Key</p>
	 * 
	 * @param secret DH Secret
	 * @param skey InfoHash
	 * 
	 * @return Key
	 */
	private static final Key buildRecvKey(byte[] secret, byte[] skey) {
		return buildKey(KEY_RECV, secret, skey);
	}

	/**
	 * <p>新建Key</p>
	 * 
	 * @param key keyA | keyB
	 * @param secret DH Secret
	 * @param skey InfoHash
	 * 
	 * @return Key
	 */
	private static final Key buildKey(String key, byte[] secret, byte[] skey) {
		final MessageDigest digest = DigestUtils.sha1();
		digest.update(key.getBytes());
		digest.update(secret);
		digest.update(skey);
		return new SecretKeySpec(digest.digest(), ARC4_ALGO);
	}

	/**
	 * <p>新建Cipher</p>
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
		// 丢弃1024字节
		cipher.update(new byte[1024]);
		return cipher;
	}

}
