package com.acgist.snail.net.hls.crypt;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.acgist.snail.system.exception.ArgumentException;

/**
 * <p>HLS加密工具：AES-128</p>
 * 
 * @author acgist
 * @version 1.5.0
 */
public class HlsCryptAes128 extends HlsCrypt {
	
	/**
	 * <p>加密套件</p>
	 */
	private final Cipher cipher;

	/**
	 * @param secret 密钥
	 * @param iv IV
	 */
	private HlsCryptAes128(byte[] secret, byte[] iv) {
		final SecretKeySpec secretKeySpec = new SecretKeySpec(secret, "AES");
		final IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
		try {
			this.cipher = Cipher.getInstance("AES/CBC/NoPadding");
			this.cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
			throw new ArgumentException("不支持的算法", e);
		}
	}

	/**
	 * <p>新建加密工具</p>
	 * 
	 * @param secret 密钥
	 * @param iv IV
	 * 
	 * @return 加密工具
	 */
	public static final HlsCryptAes128 newInstance(byte[] secret, byte[] iv) {
		return new HlsCryptAes128(secret, iv);
	}
	
	@Override
	public byte[] decrypt(byte[] source) {
		return this.cipher.update(source);
	}

}
