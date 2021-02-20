package com.acgist.snail.config;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

class DownloadConfigTest extends Performance {

	@Test
	void testDownloadConfig() {
		final var config = DownloadConfig.getInstance();
		assertNotNull(config);
		assertNotEquals(0, DownloadConfig.getUploadBufferByte());
		assertNotEquals(0, DownloadConfig.getDownloadBufferByte());
		DownloadConfig.setBuffer(1024);
		assertNotEquals(1024 / 4, DownloadConfig.getUploadBufferByte());
		assertNotEquals(1024, DownloadConfig.getDownloadBufferByte());
	}
	
}
