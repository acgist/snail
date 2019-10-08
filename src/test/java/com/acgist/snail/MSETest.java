package com.acgist.snail;

import java.nio.ByteBuffer;

import org.junit.Test;

import com.acgist.snail.net.torrent.peer.bootstrap.crypt.MSEKeyPairBuilder;
import com.acgist.snail.system.config.CryptConfig;
import com.acgist.snail.utils.NumberUtils;
import com.acgist.snail.utils.StringUtils;

public class MSETest {

	@Test
	public void dh() {
		MSEKeyPairBuilder mseKeyPairBuilder = MSEKeyPairBuilder.newInstance();
		var a = mseKeyPairBuilder.buildKeyPair();
		var b = mseKeyPairBuilder.buildKeyPair();
		System.out.println("双方公钥、私钥");
		System.out.println(a.getPublic());
		System.out.println(a.getPrivate());
		System.out.println(b.getPublic());
		System.out.println(b.getPrivate());
		var aKey = NumberUtils.decodeUnsigned(ByteBuffer.wrap(a.getPublic().getEncoded()), CryptConfig.PUBLIC_KEY_LENGTH);
		var bKey = NumberUtils.decodeUnsigned(ByteBuffer.wrap(b.getPublic().getEncoded()), CryptConfig.PUBLIC_KEY_LENGTH);
		System.out.println("双方密钥");
		System.out.println(StringUtils.hex(aKey.toByteArray()));
		System.out.println(StringUtils.hex(bKey.toByteArray()));
		var as = NumberUtils.encodeUnsigned(MSEKeyPairBuilder.buildDHSecret(aKey, b.getPrivate()), CryptConfig.PUBLIC_KEY_LENGTH);
		var bs = NumberUtils.encodeUnsigned(MSEKeyPairBuilder.buildDHSecret(bKey, a.getPrivate()), CryptConfig.PUBLIC_KEY_LENGTH);
		System.out.println("加密S");
		System.out.println(StringUtils.hex(as));
		System.out.println(StringUtils.hex(bs));
	}
	
}
