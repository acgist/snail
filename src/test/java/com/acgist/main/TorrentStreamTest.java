package com.acgist.main;

import java.nio.ByteBuffer;

import org.junit.Test;

import com.acgist.snail.downloader.torrent.TorrentStream;
import com.acgist.snail.downloader.torrent.bean.TorrentPiece;
import com.acgist.snail.system.exception.DownloadException;

public class TorrentStreamTest {
	
	private TorrentStream stream = new TorrentStream(1024 * 1024, 10);

	@Test
	public void test() throws DownloadException {
		stream.newFile("e://x.x", 100L * 1024 * 1024, 0);
		TorrentPiece piece = new TorrentPiece();
//		byte[] bytes = "0000".getBytes();
		byte[] bytes = ByteBuffer.allocate(4).putInt(0).array();
		piece.setIndex(0);
		piece.setBegin(10);
		piece.setLength(bytes.length);
		piece.setData(bytes);
		stream.pieces(piece);
		stream.release();
	}
	
	@Test
	public void read() throws DownloadException {
		System.out.println((byte) 0);
		stream.newFile("e://x.x", 100L * 1024 * 1024, 0);
		byte[] bytes = stream.read(0);
		int index = 0;
		for (byte b : bytes) {
			System.out.println(b);
			if(index++ > 10) {
				break;
			}
		}
	}
	
}
