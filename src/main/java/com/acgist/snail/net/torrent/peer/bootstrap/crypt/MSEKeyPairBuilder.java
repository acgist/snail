package com.acgist.snail.net.torrent.peer.bootstrap.crypt;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Random;

import com.acgist.snail.system.config.CryptConfig;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.exception.ArgumentException;
import com.acgist.snail.utils.NumberUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>
 * MSE密钥对Builder（DH交换）
 * </p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class MSEKeyPairBuilder {

	/**
	 * 公钥生成使用随机数
	 */
	private final Random random;

	private MSEKeyPairBuilder() {
		this.random = NumberUtils.random();
	}
	
	public static final MSEKeyPairBuilder newInstance() {
		return new MSEKeyPairBuilder();
	}

	/**
	 * 创建密钥对
	 */
	public KeyPair buildKeyPair() {
		final MSEPrivateKey privateKey = new MSEPrivateKey(this.random);
		final MSEPublicKey publicKey = privateKey.getPublicKey();
		return new KeyPair(publicKey, privateKey);
	}

	/**
	 * 创建S：DH Secret
	 */
	public static final BigInteger buildDHSecret(BigInteger publicKey, PrivateKey privateKey) {
		if(privateKey instanceof MSEPrivateKey) {
			return ((MSEPrivateKey) privateKey).buildDHSecret(new MSEPublicKey(publicKey));
		}
		throw new ArgumentException("不支持的私钥：" + privateKey);
	}

	/**
	 * MSE公钥
	 */
	private static final class MSEPublicKey implements PublicKey {

		private static final long serialVersionUID = 1L;
		
		private final Object lock;
		private final BigInteger value;
		private volatile byte[] encoded;

		private MSEPublicKey(BigInteger value) {
			this.lock = new Object();
			this.value = value;
		}

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
			if (this.encoded == null) {
				synchronized (this.lock) {
					if (this.encoded == null) {
						this.encoded = NumberUtils.encodeUnsigned(this.value, CryptConfig.PUBLIC_KEY_LENGTH);
					}
				}
			}
			return this.encoded;
		}
		
		@Override
		public String toString() {
			return StringUtils.hex(this.getEncoded());
		}
		
	}

	/**
	 * MSE私钥
	 */
	private static class MSEPrivateKey implements PrivateKey {

		private static final long serialVersionUID = 1L;
		
		private final Object lock;
		private final BigInteger value; // privateKey
		private volatile MSEPublicKey publicKey;

		private MSEPrivateKey(Random random) {
			this.lock = new Object();
			this.value = buildPrivateKey(random);
		}

		/**
		 * Xa Xb
		 */
		private BigInteger buildPrivateKey(Random random) {
			final byte[] bytes = new byte[CryptConfig.PRIVATE_KEY_LENGTH];
			for (int index = 0; index < CryptConfig.PRIVATE_KEY_LENGTH; index++) {
				bytes[index] = (byte) random.nextInt(SystemConfig.UNSIGNED_BYTE_MAX);
			}
			return NumberUtils.decodeUnsigned(ByteBuffer.wrap(bytes), CryptConfig.PRIVATE_KEY_LENGTH);
		}

		/**
		 * Pubkey of A: Ya = (G^Xa) mod P
		 * Pubkey of B: Yb = (G^Xb) mod P
		 */
		private MSEPublicKey getPublicKey() {
			if (this.publicKey == null) {
				synchronized (this.lock) {
					if (this.publicKey == null) {
						this.publicKey = new MSEPublicKey(CryptConfig.G.modPow(this.value, CryptConfig.P));
					}
				}
			}
			return this.publicKey;
		}

		/**
		 * DH secret: S = (Ya^Xb) mod P = (Yb^Xa) mod P
		 */
		public BigInteger buildDHSecret(MSEPublicKey publicKey) {
			return publicKey.getValue().modPow(this.value, CryptConfig.P);
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
