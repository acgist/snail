package com.acgist.snail.net.torrent.peer.bootstrap.crypt;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import com.acgist.snail.system.config.CryptConfig;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.exception.ArgumentException;
import com.acgist.snail.utils.NumberUtils;

/**
 * <p>
 * MSE密钥对Builder（DH交换）
 * </p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class MSEKeyPairBuilder {

	private final SecureRandom random;

	private MSEKeyPairBuilder() {
		this.random = new SecureRandom();
	}
	
	public static final MSEKeyPairBuilder newInstance() {
		return new MSEKeyPairBuilder();
	}

	public KeyPair buildKeyPair() {
		final MSEPrivateKey privateKey = new MSEPrivateKey(this.random);
		final MSEPublicKey publicKey = privateKey.getPublicKey();
		return new KeyPair(publicKey, privateKey);
	}

	public BigInteger buildDHSecret(BigInteger publicKey, PrivateKey privateKey) {
		if(privateKey instanceof MSEPrivateKey) {
			return ((MSEPrivateKey) privateKey).buildDHSecret(new MSEPublicKey(publicKey));
		}
		throw new ArgumentException("不支持的PrivateKey：" + privateKey);
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
						this.encoded = NumberUtils.encodeUnsigned(this.value, CryptConfig.PUBLIC_KEY_SIZE);
					}
				}
			}
			return this.encoded;
		}
		
	}

	/**
	 * MSE私钥
	 */
	private static class MSEPrivateKey implements PrivateKey {

		private static final long serialVersionUID = 1L;
		
		private final Object lock;
		private final BigInteger value;
		private volatile MSEPublicKey publicKey;

		private MSEPrivateKey(SecureRandom random) {
			this.lock = new Object();
			this.value = buildPrivateKey(random);
		}

		/**
		 * Xa Xb
		 */
		private BigInteger buildPrivateKey(SecureRandom random) {
			final byte[] bytes = new byte[CryptConfig.PRIVATE_KEY_SIZE];
			for (int index = 0; index < CryptConfig.PRIVATE_KEY_SIZE; index++) {
				bytes[index] = (byte) random.nextInt(SystemConfig.UNSIGNED_BYTE_SIZE);
			}
			return NumberUtils.decodeUnsigned(ByteBuffer.wrap(bytes), CryptConfig.PRIVATE_KEY_SIZE);
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
			return null;
		}

		@Override
		public byte[] getEncoded() {
			return null;
		}

	}

}
