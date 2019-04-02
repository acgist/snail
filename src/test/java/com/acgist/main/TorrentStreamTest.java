package com.acgist.main;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.BitSet;

import org.junit.Test;

import com.acgist.snail.downloader.torrent.TorrentStream;
import com.acgist.snail.downloader.torrent.bean.TorrentPiece;
import com.acgist.snail.utils.FileUtils;

public class TorrentStreamTest {

	@Test
	public void bitSet() {
		BitSet bit = new BitSet();
		bit.set(0, true);
		bit.set(9, true);
		System.out.println(bit.nextSetBit(0));
		System.out.println(bit.nextClearBit(0));
		System.out.println(bit.size());
		System.out.println(bit.length());
		bit.stream().forEach(System.out::println);
	}

	@Test
	public void newFile() throws IOException {
		String file = "e://resteasy-jaxrs-3.0.13.Final-all-corrected.zip";
		TorrentStream stream = new TorrentStream(1024 * 1024, 100);
		stream.newFile(file, FileUtils.fileSize(file), 100);
	}

	@Test
	public void write() throws IOException {
		String file = "e://torrent.piece";
		TorrentStream stream = new TorrentStream(1024 * 1024, 1024);
		stream.newFile(file, 1024L * 1024 * 1024, 100);
		TorrentPiece piece = new TorrentPiece();
		piece.setIndex(0);
		piece.setBegin(104);
		ByteBuffer buffer = ByteBuffer.wrap("test".getBytes());
		piece.setData(buffer.array());
		piece.setLength(4);
		stream.pieces(piece);
		stream.release();
	}

}
