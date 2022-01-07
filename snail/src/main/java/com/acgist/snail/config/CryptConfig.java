package com.acgist.snail.config;

import java.math.BigInteger;

import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;

/**
 * <p>数据流加密（MSE）配置</p>
 * 
 * @author acgist
 */
public final class CryptConfig {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CryptConfig.class);
	
	/**
	 * <p>加密算法</p>
	 * 
	 * @author acgist
	 */
	public enum CryptAlgo {
		
		/**
		 * <p>明文</p>
		 */
		PLAINTEXT(0x01),
		/**
		 * <p>ARC4</p>
		 */
		ARC4(0x02);
		
		/**
		 * <p>provide</p>
		 */
		private final int provide;
		
		/**
		 * @param provide provide
		 */
		private CryptAlgo(int provide) {
			this.provide = provide;
		}

		/**
		 * <p>获取provide</p>
		 * 
		 * @return provide
		 */
		public final int provide() {
			return this.provide;
		}
		
	}
	
	/**
	 * <p>加密策略</p>
	 * 
	 * @author acgist
	 */
	public enum Strategy {
		
		/**
		 * <p>明文</p>
		 */
		PLAINTEXT(false, CryptAlgo.PLAINTEXT.provide),
		/**
		 * <p>偏爱明文</p>
		 */
		PREFER_PLAINTEXT(false, CryptAlgo.PLAINTEXT.provide | CryptAlgo.ARC4.provide),
		/**
		 * <p>偏爱加密</p>
		 */
		PREFER_ENCRYPT(true, CryptAlgo.ARC4.provide | CryptAlgo.PLAINTEXT.provide),
		/**
		 * <p>加密</p>
		 */
		ENCRYPT(true, CryptAlgo.ARC4.provide);
		
		/**
		 * <p>是否加密</p>
		 */
		private final boolean crypt;
		/**
		 * <p>加密模式：crypto_provide</p>
		 * <p>支持多种加密算法：双方协商最优加密算法</p>
		 * 
		 * @see CryptAlgo#provide
		 */
		private final int provide;
		
		/**
		 * @param crypt 是否加密
		 * @param provide 加密模式
		 */
		private Strategy(boolean crypt, int provide) {
			this.crypt = crypt;
			this.provide = provide;
		}
		
		/**
		 * <p>判断是否加密</p>
		 * 
		 * @return 是否加密
		 */
		public final boolean crypt() {
			return this.crypt;
		}
		
		/**
		 * <p>获取加密模式</p>
		 * 
		 * @return 加密模式
		 */
		public final int provide() {
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
	 * <p>推荐长度：160</p>
	 * <p>超过180只能增加计算时间：并不能提高安全性</p>
	 */
	public static final int PRIVATE_KEY_LENGTH = 128;
	/**
	 * <p>最大随机填充长度：{@value}</p>
	 */
	public static final int PADDING_MAX_LENGTH = 512;
	/**
	 * <p>VC数据</p>
	 * <p>默认填充：0x00</p>
	 */
	public static final byte[] VC = {0, 0, 0, 0, 0, 0, 0, 0};
	/**
	 * <p>VC长度</p>
	 * 
	 * @see #VC
	 */
	public static final int VC_LENGTH = VC.length;
	/**
	 * <p>默认加密策略</p>
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
