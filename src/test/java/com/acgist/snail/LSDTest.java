package com.acgist.snail;

import org.junit.Test;

import com.acgist.snail.net.torrent.local.LocalServiceDiscoveryClient;
import com.acgist.snail.net.torrent.local.LocalServiceDiscoveryServer;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.ThreadUtils;

public class LSDTest {

	@Test
	public void server() {
		LocalServiceDiscoveryServer.getInstance();
		ThreadUtils.sleep(Long.MAX_VALUE);
	}
	
	@Test
	public void client() throws NetException {
		var client = LocalServiceDiscoveryClient.newInstance();
		client.localSearch("xxxx");
	}
	
}
