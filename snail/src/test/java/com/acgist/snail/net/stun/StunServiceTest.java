package com.acgist.snail.net.stun;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.ThreadUtils;

class StunServiceTest extends Performance {

	@Test
	void testMapping() {
		final StunService service = StunService.getInstance();
		int index = 0;
		while(index++ < 5 && SystemConfig.getExternalIPAddress() == null) {
			service.mapping();
			ThreadUtils.sleep(2000);
		}
		assertNotNull(SystemConfig.getExternalIPAddress());
	}
	
}
