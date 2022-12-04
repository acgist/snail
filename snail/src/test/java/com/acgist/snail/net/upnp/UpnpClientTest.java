package com.acgist.snail.net.upnp;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.ThreadUtils;

class UpnpClientTest extends Performance {

	@Test
	void testMSearch() {
		UpnpClient.newInstance().mSearch();
		ThreadUtils.sleep(1000);
		assertTrue(UpnpContext.getInstance().available());
	}

}
