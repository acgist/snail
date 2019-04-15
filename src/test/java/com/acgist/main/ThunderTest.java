package com.acgist.main;

import java.util.Base64;

import org.junit.Test;

import com.acgist.snail.protocol.thunder.ThunderProtocol;

public class ThunderTest {

	@Test
	public void test() {
		String url = "thunder://QUFmdHA6Ly95Z2R5ODp5Z2R5OEB5ZzQ1LmR5ZHl0dC5uZXQ6NDEyMi/R9LnitefTsHd3dy55Z2R5OC5jb20usNfJ36O61LXG8C5IRC43MjBwLrn60+/W0NfWLm1rdlpa";
		url = url.substring(ThunderProtocol.THUNDER_PREFIX.length());
		String newUrl = new String(Base64.getDecoder().decode(url));
		newUrl = newUrl.substring(2, newUrl.length() - 2);
		
		System.out.println(newUrl);
	}
	
}
