package com.acgist.snail.net.torrent.codec;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import com.acgist.snail.config.CryptConfig;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.NumberUtils;

/**
 * MSE密钥对Builder
 * 一的补码（one's complement）：反码（正数=原码、负数=反码）
 * 二的补码（two's complement）：补码（正数=原码、负数=反码+1）
 * 
 * @author acgist
 */
public final class MSEKeyPairBuilder {
    
    /**
     * 算法：{@value}
     */
    private static final String ALGORITHM = "DH";
    /**
     * 算法编码：{@value}
     */
    private static final String FORMAT = "MSE";

    private MSEKeyPairBuilder() {
    }
    
    /**
     * 新建MSE密钥对Builder
     * 
     * @return MSE密钥对Builder
     */
    public static final MSEKeyPairBuilder newInstance() {
        return new MSEKeyPairBuilder();
    }

    /**
     * 新建密钥对
     * 
     * @return 密钥对
     */
    public KeyPair buildKeyPair() {
        final MSEPrivateKey privateKey = new MSEPrivateKey();
        final MSEPublicKey publicKey   = privateKey.getPublicKey();
        return new KeyPair(publicKey, privateKey);
    }

    /**
     * MSE公钥
     * 
     * @author acgist
     */
    public static final class MSEPublicKey implements PublicKey {

        private static final long serialVersionUID = 1L;
        
        /**
         * 公钥数据
         */
        private final BigInteger value;

        /**
         * @param value 公钥数据
         */
        private MSEPublicKey(BigInteger value) {
            this.value = value;
        }

        @Override
        public String getAlgorithm() {
            return ALGORITHM;
        }

        @Override
        public String getFormat() {
            return FORMAT;
        }

        @Override
        public byte[] getEncoded() {
            return NumberUtils.encodeBigInteger(this.value, CryptConfig.PUBLIC_KEY_LENGTH);
        }
        
        @Override
        public String toString() {
            return this.value.toString();
        }
        
    }

    /**
     * MSE私钥
     * 
     * @author acgist
     */
    public static final class MSEPrivateKey implements PrivateKey {

        private static final long serialVersionUID = 1L;
        
        /**
         * 私钥数据
         */
        private final BigInteger value;
        /**
         * PublicKey
         */
        private final MSEPublicKey publicKey;

        private MSEPrivateKey() {
            this.value = this.buildPrivateKey();
            this.publicKey = this.buildPublicKey();
        }

        /**
         * 新建私钥数据
         * 
         * <pre>
         * Xa Xb
         * </pre>
         * 
         * @return 私钥数据
         */
        private BigInteger buildPrivateKey() {
            final byte[] bytes = ArrayUtils.random(CryptConfig.PRIVATE_KEY_LENGTH);
            return NumberUtils.decodeBigInteger(ByteBuffer.wrap(bytes), CryptConfig.PRIVATE_KEY_LENGTH);
        }
        
        /**
         * 新建公钥
         * 
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
         * 新建DH Secret
         * 
         * <pre>
         * DH Secret: S = (Ya^Xb) mod P = (Yb^Xa) mod P
         * </pre>
         * 
         * @param publicKey 公钥数据
         * 
         * @return DH Secret
         * 
         * @see MSEPublicKey#value
         */
        public BigInteger buildDHSecret(BigInteger publicKey) {
            return publicKey.modPow(this.value, CryptConfig.P);
        }
        
        /**
         * @return PublicKey
         */
        private MSEPublicKey getPublicKey() {
            return this.publicKey;
        }

        @Override
        public String getAlgorithm() {
            return ALGORITHM;
        }

        @Override
        public String getFormat() {
            return FORMAT;
        }

        @Override
        public byte[] getEncoded() {
            return this.value.toByteArray();
        }

        @Override
        public String toString() {
            return this.value.toString();
        }
        
    }

}
