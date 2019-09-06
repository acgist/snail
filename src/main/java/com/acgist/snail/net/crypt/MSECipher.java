package com.acgist.snail.net.crypt;

import java.nio.charset.Charset;
import java.security.Key;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.exception.ArgumentException;
import com.acgist.snail.utils.DigestUtils;

/**
 * <p>MSE加密套件（ARC4）</p>
 * <p>参考链接：https://baike.baidu.com/item/RC4/3454548</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class MSECipher {

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

	/**
	 * 请求客户端
	 * 
	 * @param S DH Secret
	 * @param infoHash infoHash
	 */
	public static final MSECipher newInitiator(byte[] S, InfoHash infoHash) {
		return new MSECipher(S, infoHash, true);
	}

	/**
	 * 连入客户端
	 * 
	 * @param S DH Secret
	 * @param infoHash infoHash
	 */
	public static final MSECipher newReceiver(byte[] S, InfoHash infoHash) {
		return new MSECipher(S, infoHash, false);
	}

	private MSECipher(byte[] S, InfoHash infoHash, boolean initiator) {
		Key initiatorKey = buildInitiatorEncryptionKey(S, infoHash.infoHash());
		Key receiverKey = buildReceiverEncryptionKey(S, infoHash.infoHash());
		Key encryptKey = initiator ? initiatorKey : receiverKey;
		Key decryptKey = initiator ? receiverKey : initiatorKey;
		this.decryptCipher = buildCipher(Cipher.DECRYPT_MODE, ARC4_ALGO_TRANSFORMATION, decryptKey);
		this.encryptCipher = buildCipher(Cipher.ENCRYPT_MODE, ARC4_ALGO_TRANSFORMATION, encryptKey);
	}

	/**
	 * 加密Cipher
	 */
	public Cipher getEncryptionCipher() {
		return this.encryptCipher;
	}

	/**
	 * 解密Cipher
	 */
	public Cipher getDecryptionCipher() {
		return this.decryptCipher;
	}

	private Key buildInitiatorEncryptionKey(byte[] S, byte[] SKEY) {
		return buildEncryptionKey("keyA", S, SKEY);
	}

	private Key buildReceiverEncryptionKey(byte[] S, byte[] SKEY) {
		return buildEncryptionKey("keyB", S, SKEY);
	}

	/**
	 * 创建KEY
	 */
	private Key buildEncryptionKey(String s, byte[] S, byte[] SKEY) {
		final MessageDigest digest = DigestUtils.sha1();
		digest.update(s.getBytes(Charset.forName(SystemConfig.CHARSET_ASCII))); // TODO：编码
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
			cipher.update(new byte[1024]);
			return cipher;
		} catch (Exception e) {
			throw new ArgumentException(e);
		}
	}

}
