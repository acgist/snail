package com.acgist.snail.context;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import com.acgist.snail.utils.Performance;

public class LoggerContextTest extends Performance {

	@Test
	public void testGetName() {
		final String name = LoggerContext.getInstance().getName();
		assertNotNull(name);
		this.log(name);
	}
	
	@Test
	public void testGetLogger() {
		final Logger logger = LoggerContext.getInstance().getLogger("acgist");
		assertNotNull(logger);
		logger.debug("acgist");
	}

	@Test
	public void testShutdown() {
		this.log("close");
		LoggerContext.shutdown();
		assertDoesNotThrow(() -> this.log("close"));
	}
	
}
