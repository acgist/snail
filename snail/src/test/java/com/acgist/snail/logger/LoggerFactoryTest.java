package com.acgist.snail.logger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

class LoggerFactoryTest extends Performance {

	@Test
	void testGetLogger() {
		final Logger logger = LoggerFactory.getLogger(LoggerFactoryTest.class);
		assertNotNull(logger);
		logger.debug("acgist");
	}

	@Test
	void testShutdown() {
		this.log("close");
		LoggerFactory.shutdown();
		assertDoesNotThrow(() -> this.log("close"));
	}
	
}
