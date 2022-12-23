package com.acgist.snail.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.Performance;

class DownloadConfigTest extends Performance {

	@Test
	void testDownloadConfig() {
		final var config = DownloadConfig.getInstance();
		assertNotNull(config);
		assertNotEquals(0, DownloadConfig.getUploadBufferByte());
		assertNotEquals(0, DownloadConfig.getDownloadBufferByte());
		final int buffer = 2048;
		DownloadConfig.setBuffer(buffer);
		assertNotEquals(buffer / 4, DownloadConfig.getUploadBufferByte());
		assertNotEquals(buffer, DownloadConfig.getDownloadBufferByte());
		assertEquals(16, DownloadConfig.getMemoryBufferByte(16));
		assertEquals(DownloadConfig.getMemoryBufferByte(), DownloadConfig.getMemoryBufferByte(1024 * 1024 * 1024));
		assertTrue(FileUtils.userDirFile(DownloadConfig.DOWNLOAD_CONFIG).exists());
	}

	@Test
	void testPath() {
		this.log("{}", DownloadConfig.getPath());
		this.log("{}", DownloadConfig.getLastPath());
	}
	
}
