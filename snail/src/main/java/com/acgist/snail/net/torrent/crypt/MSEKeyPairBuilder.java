package com.acgist.snail.net.torrent.crypt;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Random;

import com.acgist.snail.config.CryptConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.utils.NumberUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>MSE密钥对Builder（DH交换）</p>
 * <p>一的补码（one's complement）：反码（正数=原码、负数=反码）</p>
 * <p>二的补码（two's complement）：补码（正数=原码、负数=反码+{@code 1}）</p>
 * 
 * @author acgist
 */
public final class MSEKeyPairBuilder {

	/**
	 * <p>公钥生成使用随机数</p>
	 */
	private final Random random;

	private MSEKeyPairBuilder() {
		this.random = NumberUtils.random();
	}
	
	/**
	 * <p>创建MSE密钥对Builder</p>
	 * 
	 * @return MSE密钥对Builder
	 */
	public static final MSEKeyPairBuilder newInstance() {
		return new MSEKeyPairBuilder();
	}

	/**
	 * <p>创建密钥对</p>
	 * 
	 * @return 密钥对
	 */
	public KeyPair buildKeyPair() {
		final MSEPrivateKey privateKey = new MSEPrivateKey(this.random);
		final MSEPublicKey publicKey = privateKey.getPublicKey();
		return new KeyPair(publicKey, privateKey);
	}

	/**
	 * <p>创建S：DH Secret</p>
	 * 
	 * @param publicKey 公钥
	 * @param privateKey 密钥
	 * 
	 * @return DH Secret
	 * 
	 * @throws InvalidKeyException 密钥异常
	 */
	public static final BigInteger buildDHSecret(BigInteger publicKey, PrivateKey privateKey) throws InvalidKeyException {
		if(privateKey instanceof MSEPrivateKey) {
			return ((MSEPrivateKey) privateKey).buildDHSecret(new MSEPublicKey(publicKey));
		}
		throw new InvalidKeyException("不支持的私钥：" + privateKey);
	}

	/**
	 * <p>MSE公钥</p>
	 * 
	 * @author acgist
	 */
	private static final class MSEPublicKey implements PublicKey {

		private static final long serialVersionUID = 1L;
		
		/**
		 * <p>PublicKey</p>
		 */
		private final BigInteger value;
		/**
		 * <p>公钥数据</p>
		 */
		private final byte[] encoded;

		/**
		 * @param value PublicKey
		 */
		private MSEPublicKey(BigInteger value) {
			this.value = value;
			this.encoded = NumberUtils.encodeBigInteger(value, CryptConfig.PUBLIC_KEY_LENGTH);
		}

		/**
		 * <p>获取PublicKey</p>
		 * 
		 * @return PublicKey
		 */
		public BigInteger getValue() {
			return this.value;
		}

		@Override
		public String getAlgorithm() {
			return "DH";
		}

		@Override
		public String getFormat() {
			return "MSE";
		}

		@Override
		public byte[] getEncoded() {
			return this.encoded;
		}
		
		@Override
		public String toString() {
			return StringUtils.hex(this.getEncoded());
		}
		
	}

	/**
	 * <p>MSE私钥</p>
	 * 
	 * @author acgist
	 */
	private static final class MSEPrivateKey implements PrivateKey {

		private static final long serialVersionUID = 1L;
		
		/**
		 * <p>PrivateKey</p>
		 */
		private final BigInteger value;
		/**
		 * <p>PublicKey</p>
		 */
		private final MSEPublicKey publicKey;

		/**
		 * @param random 随机数工具
		 */
		private MSEPrivateKey(Random random) {
			this.value = this.buildPrivateKey(random);
			this.publicKey = this.buildPublicKey();
		}

		/**
		 * <p>Xa Xb</p>
		 * 
		 * @param random 随机数工具
		 * 
		 * @return PrivateKey
		 */
		private BigInteger buildPrivateKey(Random random) {
			final byte[] bytes = new byte[CryptConfig.PRIVATE_KEY_LENGTH];
			for (int index = 0; index < CryptConfig.PRIVATE_KEY_LENGTH; index++) {
				bytes[index] = (byte) random.nextInt(SystemConfig.UNSIGNED_BYTE_MAX);
			}
			return NumberUtils.decodeBigInteger(ByteBuffer.wrap(bytes), CryptConfig.PRIVATE_KEY_LENGTH);
		}
		
		/**
		 * <pre>
		 * Pubkey of A: Ya = (G^Xa) mod P
		 * Pubkey of B: Yb = (G^Xb) mod P
		 * </pre>
		 * 
		 * @return PublicKey
		 */
		private MSEPublicKey buildPublicKey() {
			return new MSEPublicKey(CryptConfig.G.modPow(this.value, CryptConfig.P));
		}
		
		/**
		 * <p>DH Secret: S = (Ya^Xb) mod P = (Yb^Xa) mod P</p>
		 * 
		 * @param publicKey PublicKey
		 * 
		 * @return DH Secret
		 */
		public BigInteger buildDHSecret(MSEPublicKey publicKey) {
			return publicKey.getValue().modPow(this.value, CryptConfig.P);
		}
		
		/**
		 * <p>获取PublicKey</p>
		 * 
		 * @return PublicKey
		 */
		private MSEPublicKey getPublicKey() {
			return this.publicKey;
		}

		@Override
		public String getAlgorithm() {
			return "DH";
		}

		@Override
		public String getFormat() {
			return "MSE";
		}

		@Override
		public byte[] getEncoded() {
			return this.value.toByteArray();
		}

		@Override
		public String toString() {
			return StringUtils.hex(this.getEncoded());
		}
		
	}

}
