package com.acgist.snail.net.crypto;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import com.acgist.snail.system.config.CryptoConfig;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.NumberUtils;

/**
 * <p>
 * MSE密钥对（DH交换）
 * </p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class MSEKeyPairBuilder {

	private final SecureRandom random;
	private final int privateKeySize;

	private MSEKeyPairBuilder(int privateKeySize) {
		this.random = new SecureRandom();
		this.privateKeySize = privateKeySize;
	}
	
	public static final MSEKeyPairBuilder newInstance() {
		return new MSEKeyPairBuilder(CryptoConfig.PRIVATE_KEY_SIZE);
	}

	public KeyPair buildKeyPair() {
		final MSEPrivateKey privateKey = new MSEPrivateKey(this.privateKeySize, this.random);
		final MSEPublicKey publicKey = privateKey.getPublicKey();
		return new KeyPair(publicKey, privateKey);
	}

	public BigInteger buildSharedSecret(BigInteger publicKey, MSEPrivateKey privateKey) {
		return privateKey.buildSharedSecret(new MSEPublicKey(publicKey));
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
						this.encoded = NumberUtils.encodeUnsigned(this.value, CryptoConfig.PUBLIC_KEY_SIZE);
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

		private MSEPrivateKey(int privateKeySize, SecureRandom random) {
			this.lock = new Object();
			this.value = buildPrivateKey(privateKeySize, random);
		}

		private BigInteger buildPrivateKey(int privateKeySize, SecureRandom random) {
			final byte[] bytes = new byte[privateKeySize];
			for (int index = 0; index < privateKeySize; index++) {
				bytes[index] = (byte) random.nextInt(SystemConfig.UNSIGNED_BYTE_SIZE);
			}
			return NumberUtils.decodeUnsigned(ByteBuffer.wrap(bytes), privateKeySize);
		}

		private MSEPublicKey getPublicKey() {
			if (this.publicKey == null) {
				synchronized (this.lock) {
					if (this.publicKey == null) {
						this.publicKey = new MSEPublicKey(CryptoConfig.G.modPow(this.value, CryptoConfig.P));
					}
				}
			}
			return this.publicKey;
		}

		public BigInteger buildSharedSecret(MSEPublicKey publicKey) {
			return publicKey.getValue().modPow(this.value, CryptoConfig.P);
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
