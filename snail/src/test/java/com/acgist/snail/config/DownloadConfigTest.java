package com.acgist.snail.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.BaseTest;

public class DownloadConfigTest extends BaseTest {

	@Test
	public void test() {
		final var config = DownloadConfig.getInstance();
		assertNotNull(config);
	}
	
}
