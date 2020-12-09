package com.acgist.snail.net.torrent.bootstrap;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.StringUtils;

public class TorrentStreamTest extends Performance {

	private final int PIECE_SIZE = 8 * 1024 * 1024;
	
	@Test
	public void testRandomAccessFile() throws IOException {
		final RandomAccessFile file = new RandomAccessFile("E:/tmp/verify/[sdy4.net]言叶之庭 The.Garden.of.Words.2013.BluRay.720p.x264.3Audio-NowYS.mkv", "rw");
		final byte[] bytes = new byte[PIECE_SIZE];
		this.cost();
		for (int index = 0; index < 256; index++) {
			file.seek(index * PIECE_SIZE);
			file.read(bytes, 0, bytes.length);
			StringUtils.sha1(bytes);
		}
		this.costed();
		file.close();
	}
	
	@Test
	public void testFileChannel() throws IOException {
		final FileChannel file = FileChannel.open(Paths.get("E:/tmp/verify/[sdy4.net]言叶之庭 The.Garden.of.Words.2013.BluRay.720p.x264.3Audio-NowYS.mkv"), StandardOpenOption.READ, StandardOpenOption.WRITE);
		final ByteBuffer buffer = ByteBuffer.allocate(PIECE_SIZE);
		this.cost();
		for (int index = 0; index < 256; index++) {
			file.position(index * PIECE_SIZE);
			file.read(buffer);
			buffer.flip();
			final byte[] bytes = new byte[buffer.remaining()];
			buffer.get(bytes);
			StringUtils.sha1(bytes);
		}
		this.costed();
		file.close();
	}
	
	@Test
	public void testShare() throws IOException {
		final RandomAccessFile file = new RandomAccessFile("E:\\学习\\zookeeper-3.4.8.tar.gz", "r");
		file.read();
		this.pause();
		file.close();
	}
	
}
