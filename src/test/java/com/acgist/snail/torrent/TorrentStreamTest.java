package com.acgist.snail.torrent;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.junit.Test;

import com.acgist.snail.BaseTest;
import com.acgist.snail.utils.StringUtils;

public class TorrentStreamTest extends BaseTest {

	@Test
	public void testRandomAccessFile() throws IOException {
		RandomAccessFile file = new RandomAccessFile("E:/tmp/verify/[sdy4.net]言叶之庭 The.Garden.of.Words.2013.BluRay.720p.x264.3Audio-NowYS.mkv", "rw");
		int pieceSize = 8 * 1024 * 1024;
		byte[] bytes = new byte[pieceSize];
		this.cost();
		for (int index = 0; index < 256; index++) {
			file.seek(index * pieceSize);
			file.read(bytes, 0, bytes.length);
			StringUtils.sha1(bytes);
		}
		this.costed();
		file.close();
	}
	
	@Test
	public void testFileChannel() throws IOException {
		FileChannel file = FileChannel.open(Paths.get("E:/tmp/verify/[sdy4.net]言叶之庭 The.Garden.of.Words.2013.BluRay.720p.x264.3Audio-NowYS.mkv"), StandardOpenOption.READ, StandardOpenOption.WRITE);
		int pieceSize = 8 * 1024 * 1024;
		ByteBuffer buffer = ByteBuffer.allocate(pieceSize);
		this.cost();
		for (int index = 0; index < 256; index++) {
			file.position(index * pieceSize);
			file.read(buffer);
			buffer.flip();
			byte[] bytes = new byte[buffer.remaining()];
			buffer.get(bytes);
			StringUtils.sha1(bytes);
		}
		this.costed();
		file.close();
	}
	
}
