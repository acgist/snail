package com.acgist.snail.context;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.net.NatContext;
import com.acgist.snail.net.NatContext.Type;
import com.acgist.snail.net.NatInitializer;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.ThreadUtils;

class NatInitializerTest extends Performance {

	@Test
	void testNatInitializer() {
		NatInitializer.newInstance().sync();
		if(NatContext.getInstance().type() != Type.UPNP) {
			ThreadUtils.sleep(2000);
		}
		assertNotNull(SystemConfig.getExternalIPAddress());
		NatContext.getInstance().shutdown();
	}
	
}
