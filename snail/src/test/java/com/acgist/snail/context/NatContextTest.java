package com.acgist.snail.context;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.NatContext.Type;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.ThreadUtils;

public class NatContextTest extends Performance {

	@Test
	public void testInit() {
		NatContext.getInstance().init();
		if(NatContext.getInstance().type() != Type.UPNP) {
			ThreadUtils.sleep(2000);
		}
		assertNotNull(SystemConfig.getExternalIpAddress());
		NatContext.getInstance().shutdown();
	}
	
}
