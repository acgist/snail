package com.acgist.snail.system.config;

import java.math.BigInteger;

/**
 * <p>加密配置</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class CryptoConfig {
	
	/**
	 * Prime P(768 bit safe prime)
	 */
	public static final BigInteger P = new BigInteger(
		"FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A63A36210000000000090563",
		16);

	/**
	 * Generator G
	 */
	public static final BigInteger G = BigInteger.valueOf(2);

	/**
	 * 公钥长度（握手终止最短长度）
	 */
	public static final int PUBLIC_KEY_SIZE = 96;
	/**
	 * 私钥长度
	 */
	public static final int PRIVATE_KEY_SIZE = 20;
	/**
	 * 填充最大长度
	 */
	public static final int MSE_MAX_PADDING = 512;

	/**
	 * 加密策略
	 */
	public enum Strategy {
		
		plaintext, // 文本
		preferPlaintext, // 兼容：偏爱文本
		preferEncrypt, // 兼容：偏爱加密
		encrypt; // 加密
		
	}
	
	/**
	 * 加密模式
	 */
	public enum CryptoProvide {
		
		plaintext(0x01), // 明文
		arc4(	  0x02); // ARC4加密
		
		int value;
		
		CryptoProvide(int value) {
			this.value = value;
		}

	}
	
}
