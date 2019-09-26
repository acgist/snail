package com.acgist.snail.system.config;

import java.math.BigInteger;

/**
 * <p>加密配置</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class CryptConfig {
	
	/**
	 * Prime P(768 bit safe prime)
	 */
	public static final BigInteger P = new BigInteger(
		"FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A63A36210000000000090563",
		16
	);

	/**
	 * Generator G
	 */
	public static final BigInteger G = BigInteger.valueOf(2);

	/**
	 * 公钥长度（握手终止最短长度）
	 */
	public static final int PUBLIC_KEY_LENGTH = 96;
	/**
	 * <p>私钥长度</p>
	 * <p>随机长度：128~180，超过180只能增加计算时间，并不能提高安全性，推荐长度：160。</p>
	 */
	public static final int PRIVATE_KEY_LENGTH = 20;
	/**
	 * 填充最大长度
	 */
	public static final int PADDING_MAX_LENGTH = 512;
	/**
	 * VC长度
	 */
	public static final int VC_LENGTH = 8;
	/**
	 * 八字节：0x00
	 */
	public static final byte[] VC = new byte[VC_LENGTH];
	/**
	 * 加密策略
	 * TODO：修改偏爱明文
	 */
	public static final Strategy STRATEGY = Strategy.preferEncrypt;
	
	/**
	 * 加密模式
	 */
	public enum CryptProvide {
		
		/** 明文 */
		plaintext(0x01),
		/** ARC4 */
		arc4(	  0x02);
		
		int value;
		
		CryptProvide(int value) {
			this.value = value;
		}

	}
	
	/**
	 * 加密策略
	 */
	public enum Strategy {
		
		/** 明文 */
		plaintext		(false, CryptProvide.plaintext.value),
		/** 兼容：偏爱明文 */
		preferPlaintext	(false, CryptProvide.plaintext.value | CryptProvide.arc4.value),
		/** 兼容：偏爱加密 */
		preferEncrypt	(true,  CryptProvide.arc4.value | CryptProvide.plaintext.value),
		/** 加密 */
		encrypt			(true,  CryptProvide.arc4.value);
		
		/**
		 * 加密
		 */
		private boolean crypt;
		/**
		 * crypto_provide
		 */
		private int provide;
		
		Strategy(boolean crypt, int provide) {
			this.crypt = crypt;
			this.provide = provide;
		}
		
		public boolean crypt() {
			return this.crypt;
		}
		
		public int provide() {
			return this.provide;
		}
		
	}
	
}
