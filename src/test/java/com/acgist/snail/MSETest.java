package com.acgist.snail;

import java.nio.ByteBuffer;

import org.junit.Test;

import com.acgist.snail.net.torrent.crypt.MSEKeyPairBuilder;
import com.acgist.snail.system.config.CryptConfig;
import com.acgist.snail.utils.NumberUtils;
import com.acgist.snail.utils.StringUtils;

public class MSETest extends BaseTest {

	@Test
	public void dh() {
		MSEKeyPairBuilder mseKeyPairBuilder = MSEKeyPairBuilder.newInstance();
		var a = mseKeyPairBuilder.buildKeyPair();
		var b = mseKeyPairBuilder.buildKeyPair();
		this.log("双方公钥、私钥");
		this.log(a.getPublic());
		this.log(a.getPrivate());
		this.log(b.getPublic());
		this.log(b.getPrivate());
		var aKey = NumberUtils.decodeUnsigned(ByteBuffer.wrap(a.getPublic().getEncoded()), CryptConfig.PUBLIC_KEY_LENGTH);
		var bKey = NumberUtils.decodeUnsigned(ByteBuffer.wrap(b.getPublic().getEncoded()), CryptConfig.PUBLIC_KEY_LENGTH);
		this.log("双方密钥");
		this.log(StringUtils.hex(aKey.toByteArray()));
		this.log(StringUtils.hex(bKey.toByteArray()));
		var as = NumberUtils.encodeUnsigned(MSEKeyPairBuilder.buildDHSecret(aKey, b.getPrivate()), CryptConfig.PUBLIC_KEY_LENGTH);
		var bs = NumberUtils.encodeUnsigned(MSEKeyPairBuilder.buildDHSecret(bKey, a.getPrivate()), CryptConfig.PUBLIC_KEY_LENGTH);
		this.log("加密S");
		this.log(StringUtils.hex(as));
		this.log(StringUtils.hex(bs));
	}
	
}
