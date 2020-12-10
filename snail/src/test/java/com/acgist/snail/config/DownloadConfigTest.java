package com.acgist.snail.config;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

public class DownloadConfigTest extends Performance {

	@Test
	public void test() {
		final var config = DownloadConfig.getInstance();
		assertNotNull(config);
		assertNotEquals(0, DownloadConfig.getUploadBufferByte());
		assertNotEquals(0, DownloadConfig.getDownloadBufferByte());
	}
	
}
