package com.acgist.snail.context;

import org.junit.jupiter.api.Test;

import com.acgist.snail.BaseTest;
import com.acgist.snail.context.exception.NetException;

public class SystemContextTest extends BaseTest {

	@Test
	public void testInfo() {
		SystemContext.info();
	}
	
	@Test
	public void latestRelease() throws NetException {
		this.log("是否最新版本：{}", SystemContext.latestRelease());
	}
	
}
