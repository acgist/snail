package com.acgist.snail.config;

import java.math.BigInteger;

import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;

/**
 * 加密配置
 * 
 * @author acgist
 */
public final class CryptConfig {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CryptConfig.class);
	
	/**
	 * 加密算法
	 * 
	 * @author acgist
	 */
	public enum CryptAlgo {
		
		/**
		 * 明文
		 */
		PLAINTEXT(0x01),
		/**
		 * ARC4
		 */
		ARC4(0x02);
		
		/**
		 * 加密算法provide
		 */
		private final int provide;
		
		/**
		 * @param provide 加密算法provide
		 */
		private CryptAlgo(int provide) {
			this.provide = provide;
		}

		/**
		 * @return 加密算法provide
		 */
		public final int provide() {
			return this.provide;
		}
		
	}
	
	/**
	 * 加密策略
	 * 
	 * @author acgist
	 */
	public enum Strategy {
		
		/**
		 * 明文
		 */
		PLAINTEXT(false, CryptAlgo.PLAINTEXT.provide),
		/**
		 * 偏爱明文
		 * 对方客户端支持明文和加密时优先使用明文传输
		 */
		PREFER_PLAINTEXT(false, CryptAlgo.PLAINTEXT.provide | CryptAlgo.ARC4.provide),
		/**
		 * 偏爱加密
		 * 对方客户端支持明文和加密时优先使用加密传输
		 */
		PREFER_ENCRYPT(true, CryptAlgo.ARC4.provide | CryptAlgo.PLAINTEXT.provide),
		/**
		 * 加密
		 */
		ENCRYPT(true, CryptAlgo.ARC4.provide);
		
		/**
		 * 是否加密
		 */
		private final boolean crypt;
		/**
		 * 加密算法provide
		 * 客户端支持多种加密算法时双方协商最优算法
		 * 
		 * @see CryptAlgo#provide
		 */
		private final int provide;
		
		/**
		 * @param crypt 是否加密
		 * @param provide 加密算法provide
		 */
		private Strategy(boolean crypt, int provide) {
			this.crypt = crypt;
			this.provide = provide;
		}
		
		/**
		 * @return 是否加密
		 */
		public final boolean crypt() {
			return this.crypt;
		}
		
		/**
		 * @return 加密模式
		 */
		public final int provide() {
			return this.provide;
		}
		
	}
	
	/**
	 * Prime P(768 bit safe prime)
	 * MSE加密算法计算公钥常量（模）
	 */
	public static final BigInteger P = new BigInteger(
		"FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A63A36210000000000090563",
		16
	);
	/**
	 * Generator G
	 * MSE加密算法计算公钥底数
	 */
	public static final BigInteger G = BigInteger.valueOf(2);
	/**
	 * 公钥长度：{@value}
	 */
	public static final int PUBLIC_KEY_LENGTH = 96;
	/**
	 * 私钥长度：{@value}
	 * 随机长度：128~180
	 * 推荐长度：160（超过180只能增加计算时间并不能提高安全性）
	 */
	public static final int PRIVATE_KEY_LENGTH = 128;
	/**
	 * 最大随机填充长度：{@value}
	 */
	public static final int PADDING_MAX_LENGTH = 512;
	/**
	 * VC数据（默认填充：0x00）
	 */
	public static final byte[] VC = {0, 0, 0, 0, 0, 0, 0, 0};
	/**
	 * VC长度
	 * 
	 * @see #VC
	 */
	public static final int VC_LENGTH = VC.length;
	/**
	 * 默认加密策略
	 * 
	 * @see Strategy
	 */
	public static final Strategy STRATEGY = Strategy.PREFER_PLAINTEXT;

	static {
		LOGGER.debug("默认加密策略：{}", CryptConfig.STRATEGY);
	}
	
	private CryptConfig() {
	}
	
}
