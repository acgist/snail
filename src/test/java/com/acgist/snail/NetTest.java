package com.acgist.snail;

import org.junit.Test;

import com.acgist.snail.net.application.ApplicationClient;
import com.acgist.snail.pojo.message.ApplicationMessage;
import com.acgist.snail.utils.ThreadUtils;

public class NetTest {

	@Test
	public void ip() {
		ApplicationClient client = ApplicationClient.newInstance();
		final var ok = client.connect();
		client.send(ApplicationMessage.text("测试"));
		System.out.println(ok);
		ThreadUtils.sleep(10000);
	}
	
}
