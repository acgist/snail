package com.acgist.main;

import java.io.FileNotFoundException;

import org.junit.Test;

import com.acgist.snail.downloader.torrent.bootstrap.TorrentStreamGroup;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.manager.TorrentSessionManager;

public class TorrentStreamGroupTest {

	@Test
	public void test() throws DownloadException, FileNotFoundException {
		String path = "e:/snail/82309348090ecbec8bf509b83b30b78a8d1f6454.torrent";
//		String path = "e:/snail/9d3854d8c6280049e5d85e490ff07de7c2bd96a2.torrent";
//		String path = "e:/snail/868f1199b18d05bf103aa8a8321f6428854d712e.torrent";
//		String path = "e:/snail/11e38a5270e15c60534ca48977b7d77a3c4f6340.torrent";
//		String path = "e:/snail/c15417e6aeab33732a59085d826edd29978f9afa.torrent";
		TorrentSession session = TorrentSessionManager.getInstance().buildSession(path);
		var files = session.torrent().getInfo().files();
		files.forEach(file -> {
			file.select(true);
		});
		TorrentStreamGroup group = TorrentStreamGroup.newInstance("e://tmp", session.torrent(), files);
//		TorrentPiece piece = new TorrentPiece();
//		piece.setIndex(0);
//		piece.setPos(0);
//		piece.setData("1234".getBytes());
////		ByteBuffer buffer = ByteBuffer.allocate(4);
////		buffer.putInt(1);
////		piece.setData(buffer.array());
//		piece.setLength(4);
//		group.pieces(piece);
		System.out.println(group.pieces());
		group.release();
	}
	
}
