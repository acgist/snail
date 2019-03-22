package com.acgist.main;

import java.util.Base64;

import org.junit.Test;

import com.acgist.snail.protocol.thunder.ThunderProtocol;

public class ThunderTest {

	@Test
	public void test() {
		String url = "thunder://ZGRmdHA6Ly9sb2NhbGhvc3QvRlRQc2VydmVyLmV4ZWNj";
		url = url.substring(ThunderProtocol.THUNDER_PREFIX.length());
		String newUrl = new String(Base64.getDecoder().decode(url));
		newUrl = newUrl.substring(2, newUrl.length() - 2);
		
		System.out.println(newUrl);
	}
	
}
