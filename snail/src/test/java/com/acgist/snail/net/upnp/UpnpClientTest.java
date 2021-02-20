package com.acgist.snail.net.upnp;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.ThreadUtils;

class UpnpClientTest extends Performance {

	@Test
	void testMSearch() {
		if(SKIP_COSTED) {
			this.log("跳过testMSearch测试");
			return;
		}
		UpnpClient.newInstance().mSearch();
		ThreadUtils.sleep(5000);
		assertNotNull(SystemConfig.getExternalIPAddress());
	}

}
