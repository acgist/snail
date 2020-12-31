package com.acgist.snail.logger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

public class LoggerConfigTest extends Performance {

	@Test
	public void testLoggerConfig() {
		assertDoesNotThrow(() -> this.log("测试"));
		LoggerConfig.off();
		assertDoesNotThrow(() -> this.log("测试"));
	}
	
}
