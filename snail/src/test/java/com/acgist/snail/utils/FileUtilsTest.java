package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

import com.acgist.snail.pojo.ITaskSession.FileType;

public class FileUtilsTest extends Performance {
	
	@Test
	public void testSystemSeparator() {
		final String truePath = "E:\\\\snail\\tmp\\\\tmp\\\\tmp";
		final String path = FileUtils.systemSeparator("E://snail\\tmp//tmp\\\\tmp");
		assertEquals(truePath, path);
	}
	
	@Test
	public void testDelete() throws IOException {
		final String path = "E://snail/tmp/" + System.currentTimeMillis();
		final File file = new File(path);
		this.log(file.getAbsolutePath());
		assertFalse(file.exists());
		Files.createFile(file.toPath());
		assertTrue(file.exists());
		FileUtils.delete(path);
		assertFalse(file.exists());
	}
	
	@Test
	public void testFileName() {
		assertEquals("订单fds.mpe", FileUtils.fileName("%e8%ae%a2%e5%8d%95fds.mpe?xx"));
		assertEquals("订单fds.mpe", FileUtils.fileName("http://acgist/%e8%ae%a2%e5%8d%95fds.mpe?xx"));
		assertEquals("view", FileUtils.fileName("https://www.acgist.com/demo/weixin/view?xx?xx"));
		assertEquals("", FileUtils.fileName("https://www.acgist.com/demo/weixin/ ?xx?xx"));
	}
	
	@Test
	public void testFileType() {
		assertEquals(FileType.VIDEO, FileUtils.fileType("http://casd/fds.mp4"));
		assertEquals(FileType.IMAGE, FileUtils.fileType("http://casd/fds.JPEG"));
		assertEquals(FileType.IMAGE, FileUtils.fileType("http://casd/fds.jPg"));
		assertEquals(FileType.UNKNOWN, FileUtils.fileType("http://casd/fds"));
		assertEquals(FileType.UNKNOWN, FileUtils.fileType("http://casd/fds.acgist"));
	}
	
	@Test
	public void testFile() {
		assertEquals("E:\\snail\\acgist", FileUtils.file("E:\\snail", "acgist"));
		assertEquals("E:\\snail\\acgist", FileUtils.file("E:\\snail\\", "acgist"));
	}
	
	@Test
	public void testFormatSize() {
		assertEquals("131.39KB", FileUtils.formatSize(134542L));
		assertEquals("1.00KB", FileUtils.formatSize(1024L));
		assertEquals("1.00M", FileUtils.formatSize(1024L * 1024));
		assertEquals("1024.00KB", FileUtils.formatSize(1024L * 1024 - 1));
	}
	
	@Test
	public void testFormatSizeMB() {
		assertEquals(0.13, FileUtils.formatSizeMB(134542L));
		assertEquals(0.0, FileUtils.formatSizeMB(1024L));
		assertEquals(1.0, FileUtils.formatSizeMB(1024L * 1024));
		assertEquals(1.0, FileUtils.formatSizeMB(1024L * 1024 - 1));
	}
	
	@Test
	public void testFileSize() {
		assertDoesNotThrow(() -> {
			final long size = FileUtils.fileSize("E:/snail/tmp");
			this.log(size);
			this.log(FileUtils.formatSize(size));
		});
	}
	
	@Test
	public void testBuildFolder() throws IOException {
		final String path = "E://snail/tmp/" + System.currentTimeMillis();
		final File file = new File(path);
		this.log(file.getAbsolutePath());
		assertFalse(file.exists());
		FileUtils.buildFolder(file);
		assertTrue(file.exists());
		FileUtils.delete(path);
		assertFalse(file.exists());
	}
	
	@Test
	public void testHash() {
		this.cost();
		FileUtils.md5("E:\\snail\\FTPserver.exe");
		assertTrue(this.costed() < 1000);
		FileUtils.sha1("E:\\\\snail\\\\FTPserver.exe");
		assertTrue(this.costed() < 1000);
	}
	
}
