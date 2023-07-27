package com.acgist.snail.net.torrent.codec;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.CryptConfig;
import com.acgist.snail.net.torrent.codec.MSEKeyPairBuilder.MSEPrivateKey;
import com.acgist.snail.utils.NumberUtils;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.StringUtils;

class MSEKeyPairBuilderTest extends Performance {

    @Test
    void testMSEKeyPairBuilder() throws InvalidKeyException {
        final MSEKeyPairBuilder mseKeyPairBuilder = MSEKeyPairBuilder.newInstance();
        final var a = mseKeyPairBuilder.buildKeyPair();
        final var b = mseKeyPairBuilder.buildKeyPair();
        this.log("双方公钥、私钥");
        this.log(a.getPublic());
        this.log(a.getPrivate());
        this.log(b.getPublic());
        this.log(b.getPrivate());
        final var aKey = NumberUtils.decodeBigInteger(ByteBuffer.wrap(a.getPublic().getEncoded()), CryptConfig.PUBLIC_KEY_LENGTH);
        final var bKey = NumberUtils.decodeBigInteger(ByteBuffer.wrap(b.getPublic().getEncoded()), CryptConfig.PUBLIC_KEY_LENGTH);
        this.log("双方密钥");
        this.log(StringUtils.hex(aKey.toByteArray()));
        this.log(StringUtils.hex(bKey.toByteArray()));
        final var as = NumberUtils.encodeBigInteger(((MSEPrivateKey) b.getPrivate()).buildDHSecret(aKey), CryptConfig.PUBLIC_KEY_LENGTH);
        final var bs = NumberUtils.encodeBigInteger(((MSEPrivateKey) a.getPrivate()).buildDHSecret(bKey), CryptConfig.PUBLIC_KEY_LENGTH);
        this.log("加密S");
        this.log(StringUtils.hex(as));
        this.log(StringUtils.hex(bs));
        assertArrayEquals(as, bs);
    }

    @Test
    void testCosted() {
        final MSEKeyPairBuilder mseKeyPairBuilder = MSEKeyPairBuilder.newInstance();
        this.costed(1000, () -> {
            try {
                final var a = mseKeyPairBuilder.buildKeyPair();
                final var b = mseKeyPairBuilder.buildKeyPair();
                final var aKey = NumberUtils.decodeBigInteger(ByteBuffer.wrap(a.getPublic().getEncoded()), CryptConfig.PUBLIC_KEY_LENGTH);
                final var bKey = NumberUtils.decodeBigInteger(ByteBuffer.wrap(b.getPublic().getEncoded()), CryptConfig.PUBLIC_KEY_LENGTH);
                final var as = NumberUtils.encodeBigInteger(((MSEPrivateKey) b.getPrivate()).buildDHSecret(aKey), CryptConfig.PUBLIC_KEY_LENGTH);
                final var bs = NumberUtils.encodeBigInteger(((MSEPrivateKey) a.getPrivate()).buildDHSecret(bKey), CryptConfig.PUBLIC_KEY_LENGTH);
                assertArrayEquals(as, bs);
            } catch (Exception e) {
                LOGGER.error("转换失败", e);
            }
        });
    }
    
}
