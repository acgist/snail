package com.acgist.snail.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

public class DatabaseConfigTest extends Performance {

	@Test
	public void test() {
		final DatabaseConfig config = DatabaseConfig.getInstance();
		assertNotNull(config);
	}
	
}
