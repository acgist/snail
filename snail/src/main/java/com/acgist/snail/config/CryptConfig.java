package com.acgist.snail.config;

import java.math.BigInteger;

import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;

public final class CryptConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(CryptConfig.class);

	public enum CryptAlgo {
		PLAINTEXT(0x01),
		ARC4(0x02);

		private final int provide;

		private CryptAlgo(int provide) {
			this.provide = provide;
		}

		public final int provide() {
			return this.provide;
		}

	}
	// used extract method in this

	public enum Strategy {
		PLAINTEXT(false, CryptAlgo.PLAINTEXT),
		PREFER_PLAINTEXT(false, CryptAlgo.PLAINTEXT, CryptAlgo.ARC4),
		PREFER_ENCRYPT(true, CryptAlgo.ARC4, CryptAlgo.PLAINTEXT),
		ENCRYPT(true,	 CryptAlgo.ARC4);

		private final boolean crypt;
		private final CryptAlgo[] algos;

		private Strategy(boolean crypt, CryptAlgo...algos) {
			this.crypt = crypt;
			this.algos = algos;
		}

		public final boolean crypt() {
			return this.crypt;
		}

		public final int provide() {
			int provide = 0;
			for(CryptAlgo algo : algos) {
				provide |= algo.provide();
			}
			return provide;
		}

	}

	public static final BigInteger P = new BigInteger(
			"FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A63A36210000000000090563",
			16
	);
	public static final BigInteger G = BigInteger.valueOf(2);
	public static final int PUBLIC_KEY_LENGTH = 96;
	public static final int PRIVATE_KEY_LENGTH = 128;
	public static final int PADDING_MAX_LENGTH = 512;
	public static final byte[] VC = {0, 0, 0, 0, 0, 0, 0, 0};
	public static final int VC_LENGTH = VC.length;
	public static final Strategy STRATEGY = Strategy.PREFER_PLAINTEXT;

	static {
		LOGGER.debug("MSE公钥长度：{}", CryptConfig.PUBLIC_KEY_LENGTH);
		LOGGER.debug("MSE私钥长度：{}", CryptConfig.PRIVATE_KEY_LENGTH);
		LOGGER.debug("MSE默认加密策略：{}", CryptConfig.STRATEGY);
	}

	private CryptConfig() {
	}

 		public static class CryptAlgoConfig {

		private final CryptAlgo algo;

		public CryptAlgoConfig(CryptAlgo algo) {
			this.algo = algo;
		}

		public final int provide() {
			return this.algo.provide();
		}

	}

	public static class StrategyConfig {

		private final Strategy strategy;

		public StrategyConfig(Strategy strategy) {
			this.strategy = strategy;
		}

		public final boolean crypt() {
			return this.strategy.crypt();
		}

		public final int provide() {
			return this.strategy.provide();
		}

	}

}
