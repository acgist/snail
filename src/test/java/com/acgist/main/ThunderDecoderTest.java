package com.acgist.main;

import org.junit.Test;

import com.acgist.snail.protocol.thunder.ThunderDecoder;

public class ThunderDecoderTest {

	@Test
	public void test() {
		String url = "thunder://QUFtYWduZXQ6P3h0PXVybjpidGloOmY5YTlhZjg0ZGI4NDMxYTcwNTIzNTZhMjNlNmY5MDg5MjYyM2Y3ZGRaWg==";
		System.out.println(ThunderDecoder.verify(url));
		System.out.println(ThunderDecoder.decode(url));
	}
	
}
