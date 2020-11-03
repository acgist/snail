package com.acgist.snail.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.junit.jupiter.api.Test;

import com.acgist.snail.BaseTest;

public class FileUtilsTest extends BaseTest {
	
	@Test
	public void testFileNameUrl() {
		this.log(FileUtils.fileName("%e8%ae%a2%e5%8d%95fds.mpe?xx"));
		this.log(FileUtils.fileName("http://acgist/%e8%ae%a2%e5%8d%95fds.mpe?xx"));
		this.log(FileUtils.fileName("https://www.acgist.com/demo/weixin/view?xx?xx"));
	}
	
	@Test
	public void testFileType() {
		this.log(FileUtils.fileType("http://casd/fds.mp4"));
		this.log(FileUtils.fileType("http://casd/fds.JPEG"));
	}
	
	@Test
	public void testFileSize() {
		long size = FileUtils.fileSize("F:\\迅雷下载\\我的大叔");
		this.log(size);
		this.log(FileUtils.formatSize(size));
		this.log(FileUtils.formatSize(1024L));
		this.log(FileUtils.formatSize(1024L * 1024));
		this.log(FileUtils.formatSize(1024L * 1024 - 1));
	}
	
	@Test
	public void testVerify() {
		this.cost();
		FileUtils.md5("F:\\迅雷下载\\我的大叔\\[我的大叔][E008].mkv").forEach((key, value) -> {
			this.log(value + "=" + key);
		});
		this.costed();
		FileUtils.sha1("F:\\迅雷下载\\我的大叔\\[我的大叔][E008].mkv").forEach((key, value) -> {
			this.log(value + "=" + key);
		});
		this.costed();
	}
	
	@Test
	public void testOrder() {
		// Java默认大端
		this.log(ByteOrder.nativeOrder()); // 系统大小端
		ByteBuffer buffer = ByteBuffer.allocate(2);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.putChar('你');
		this.log(StringUtils.hex(buffer.array()));
		buffer = ByteBuffer.allocate(2);
		buffer.putChar('你');
		this.log(StringUtils.hex(buffer.array()));
		buffer = ByteBuffer.allocate(2);
		buffer.order(ByteOrder.BIG_ENDIAN);
		buffer.putChar('你');
		this.log(StringUtils.hex(buffer.array()));
	}
	
	@Test
	public void multiple() {
		String file = "e://text.txt";
		try (
			InputStream input = new FileInputStream(file);
			OutputStream output = new FileOutputStream(file);
		) {
			output.write("1234".getBytes());
			final byte[] bytes = new byte[4];
			input.read(bytes);
			this.log(bytes);
		} catch (Exception e) {
		}
	}

	@Test
	public void fileLock() throws IOException {
		final var channel = FileChannel.open(Paths.get("e://text.txt"), StandardOpenOption.READ, StandardOpenOption.WRITE);
		channel.lock(0, Long.MAX_VALUE, true);
//		RandomAccessFile file = new RandomAccessFile("e://text.txt", "rw");
		this.pause();
	}
	
}
