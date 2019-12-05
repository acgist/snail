package com.acgist.snail;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.Test;

import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.StringUtils;

public class FileUtilsTest extends BaseTest {
	
	@Test
	public void testFileNameUrl() {
		this.log(FileUtils.fileNameFromUrl("http://casd/%e8%ae%a2%e5%8d%95fds.mpe?xx"));
		this.log(FileUtils.fileNameFromUrl("https://www.acgist.com/demo/weixin/view?xx?xx"));
	}
	
	@Test
	public void testFileType() {
		this.log(FileUtils.fileType("http://casd/fds.mp4"));
		this.log(FileUtils.fileType("http://casd/fds.JPEG"));
	}
	
	@Test
	public void testFileSize() {
		long size = FileUtils.fileSize("F:\\迅雷下载\\我的大叔\\[我的大叔][E008].mkv");
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
	
}
