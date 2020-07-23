package com.acgist.snail.system.config;

import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>MSE加密配置</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class CryptConfig {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CryptConfig.class);
	
	/**
	 * <p>加密算法</p>
	 */
	public enum CryptAlgo {
		
		/** 明文 */
		PLAINTEXT(0x01),
		/** ARC4 */
		ARC4(0x02);
		
		/**
		 * <p>provide</p>
		 */
		private final int provide;
		
		private CryptAlgo(int provide) {
			this.provide = provide;
		}

		/**
		 * <p>获取provide</p>
		 * 
		 * @return provide
		 */
		public int provide() {
			return this.provide;
		}
		
	}
	
	/**
	 * <p>加密策略</p>
	 */
	public enum Strategy {
		
		/** 明文 */
		PLAINTEXT(false, CryptAlgo.PLAINTEXT.provide),
		/** 兼容：偏爱明文 */
		PREFER_PLAINTEXT(false, CryptAlgo.PLAINTEXT.provide | CryptAlgo.ARC4.provide),
		/** 兼容：偏爱加密 */
		PREFER_ENCRYPT(true, CryptAlgo.ARC4.provide | CryptAlgo.PLAINTEXT.provide),
		/** 加密 */
		ENCRYPT(true, CryptAlgo.ARC4.provide);
		
		/**
		 * <p>是否加密</p>
		 */
		private final boolean crypt;
		/**
		 * <p>加密模式：crypto_provide</p>
		 */
		private final int provide;
		
		private Strategy(boolean crypt, int provide) {
			this.crypt = crypt;
			this.provide = provide;
		}
		
		/**
		 * <p>是否加密</p>
		 * 
		 * @return true-加密；false-不加密；
		 */
		public boolean crypt() {
			return this.crypt;
		}
		
		/**
		 * <p>获取加密模式</p>
		 * 
		 * @return 加密模式
		 */
		public int provide() {
			return this.provide;
		}
		
	}
	
	/**
	 * <p>Prime P(768 bit safe prime)</p>
	 */
	public static final BigInteger P = new BigInteger(
		"FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A63A36210000000000090563",
		16
	);
	/**
	 * <p>Generator G</p>
	 */
	public static final BigInteger G = BigInteger.valueOf(2);
	/**
	 * <p>公钥长度：{@value}</p>
	 */
	public static final int PUBLIC_KEY_LENGTH = 96;
	/**
	 * <p>私钥长度：{@value}</p>
	 * <p>随机长度：128~180</p>
	 * <p>超过180只能增加计算时间，并不能提高安全性。</p>
	 * <p>推荐长度：160</p>
	 */
	public static final int PRIVATE_KEY_LENGTH = 20;
	/**
	 * <p>填充最大随机长度：{@value}</p>
	 */
	public static final int PADDING_MAX_LENGTH = 512;
	/**
	 * <p>VC长度：{@value}</p>
	 */
	public static final int VC_LENGTH = 8;
	/**
	 * <p>VC数据：八字节（0x00）</p>
	 */
	public static final byte[] VC = new byte[VC_LENGTH];
	/**
	 * <p>默认加密策略</p>
	 */
	public static final Strategy STRATEGY = Strategy.PREFER_PLAINTEXT;

	static {
		LOGGER.info("默认加密策略：{}", CryptConfig.STRATEGY);
	}
	
	private CryptConfig() {
	}
	
}
