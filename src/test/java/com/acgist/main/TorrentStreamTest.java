package com.acgist.main;

import java.io.FileInputStream;
import java.io.IOException;

import org.junit.Test;

import com.acgist.snail.downloader.torrent.TorrentStream;
import com.acgist.snail.downloader.torrent.bean.TorrentPiece;
import com.acgist.snail.system.exception.DownloadException;

public class TorrentStreamTest {

	@Test
	public void test() throws DownloadException {
		TorrentStream stream = new TorrentStream(1024 * 1024);
		stream.newFile("e://x.x", 100L * 1024 * 1024);
		TorrentPiece piece = new TorrentPiece();
		byte[] bytes = "0000".getBytes();
		piece.setIndex(1);
		piece.setBegin(10);
		piece.setLength(bytes.length);
		piece.setData(bytes);
		stream.pieces(piece);
		stream.release();
	}
	
	@Test
	public void read() throws IOException {
		FileInputStream input = new FileInputStream("e://x.x");
		System.out.println(input.read());
	}
	
}
