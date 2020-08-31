package com.acgist.snail;

import java.util.Base64;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.hls.crypt.HlsCryptAes128;

public class HlsCryptTest extends BaseTest {

	@Test
	public void testAes128() {
		String content = "fsdFa0eSflI14gGZKoq2pX8z/k8dioVlLCPfVRrvqe0=";
		final var crypt = HlsCryptAes128.newInstance("1234567812345678".getBytes(), "1234567812345678".getBytes());
		final byte[] target = content.getBytes();
		this.log(target.length);
		this.log(target[target.length - 1]);
		final byte[] source = crypt.decrypt(Base64.getDecoder().decode(content));
		this.log(source.length);
		final int padding = source[source.length - 1];
		this.log(padding);
		this.log(new String(source, 0, source.length - padding));
	}
	
}
