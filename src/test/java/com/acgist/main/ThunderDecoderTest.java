package com.acgist.main;

import org.junit.Test;

import com.acgist.snail.protocol.thunder.ThunderProtocol;

public class ThunderDecoderTest {

	@Test
	public void test() {
		String url = "thunder://QUFtYWduZXQ6P3h0PXVybjpidGloOmY5YTlhZjg0ZGI4NDMxYTcwNTIzNTZhMjNlNmY5MDg5MjYyM2Y3ZGRaWg==";
		System.out.println(ThunderProtocol.verify(url));
		System.out.println(ThunderProtocol.decode(url));
	}
	
}
