package com.acgist.snail.net.torrent.crypt;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.CryptConfig;
import com.acgist.snail.utils.NumberUtils;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.StringUtils;

public class MSETest extends Performance {

	@Test
	public void testMSE() throws InvalidKeyException {
		MSEKeyPairBuilder mseKeyPairBuilder = MSEKeyPairBuilder.newInstance();
		var a = mseKeyPairBuilder.buildKeyPair();
		var b = mseKeyPairBuilder.buildKeyPair();
		this.log("双方公钥、私钥");
		this.log(a.getPublic());
		this.log(a.getPrivate());
		this.log(b.getPublic());
		this.log(b.getPrivate());
		var aKey = NumberUtils.decodeBigInteger(ByteBuffer.wrap(a.getPublic().getEncoded()), CryptConfig.PUBLIC_KEY_LENGTH);
		var bKey = NumberUtils.decodeBigInteger(ByteBuffer.wrap(b.getPublic().getEncoded()), CryptConfig.PUBLIC_KEY_LENGTH);
		this.log("双方密钥");
		this.log(StringUtils.hex(aKey.toByteArray()));
		this.log(StringUtils.hex(bKey.toByteArray()));
		var as = NumberUtils.encodeBigInteger(MSEKeyPairBuilder.buildDHSecret(aKey, b.getPrivate()), CryptConfig.PUBLIC_KEY_LENGTH);
		var bs = NumberUtils.encodeBigInteger(MSEKeyPairBuilder.buildDHSecret(bKey, a.getPrivate()), CryptConfig.PUBLIC_KEY_LENGTH);
		this.log("加密S");
		this.log(StringUtils.hex(as));
		this.log(StringUtils.hex(bs));
	}
	
	@Test
	public void testBigInteger() {
		byte[] value = NumberUtils.intToBytes(-1);
		this.log(value);
		value[0] = 0;
		value = new BigInteger(1, value).toByteArray();
		this.log(value);
		this.log(NumberUtils.bytesToInt(value));
	}
	
}
