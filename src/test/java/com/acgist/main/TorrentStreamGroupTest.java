package com.acgist.main;

import org.junit.Test;

import com.acgist.snail.downloader.torrent.bootstrap.TorrentStreamGroup;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.manager.TorrentManager;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.StringUtils;

public class TorrentStreamGroupTest {

	@Test
	public void test() throws DownloadException {
		String path = "e:/snail/12.torrent";
//		String path = "e:/snail/0B156834B59B0FF64EE0C9305D4D6EDE421196E6.torrent";
//		String path = "e:/snail/82309348090ecbec8bf509b83b30b78a8d1f6454.torrent";
//		String path = "e:/snail/9d3854d8c6280049e5d85e490ff07de7c2bd96a2.torrent";
//		String path = "e:/snail/868f1199b18d05bf103aa8a8321f6428854d712e.torrent";
//		String path = "e:/snail/11e38a5270e15c60534ca48977b7d77a3c4f6340.torrent";
//		String path = "e:/snail/c15417e6aeab33732a59085d826edd29978f9afa.torrent";
		TorrentSession session = TorrentManager.getInstance().newTorrentSession(path);
		var files = session.torrent().getInfo().files();
		files.forEach(file -> {
			file.select(true);
		});
		TorrentStreamGroup group = TorrentStreamGroup.newInstance("e://tmp//test", session.torrent(), files, session);
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
	
	/**
	 * 文件校验
	 */
	@Test
	public void verify() throws DownloadException {
		String path = "e:/snail/12345.torrent";
//		String path = "E:\\tmp\\[Skytree][海贼王][One_Piece][884][GB_JP][X264_AAC][720P][CRRIP][天空树双语字幕组].mp4\\[Skytree][ONE PIECE 海贼王][884][X264][720P][GB_JP][MP4][CRRIP][中日双语字幕].torrent";
		TorrentSession session = TorrentManager.getInstance().newTorrentSession(path);
		var files = session.torrent().getInfo().files();
		byte[] pieces = session.torrent().getInfo().getPieces();
		System.out.println(pieces.length);
		files.forEach(file -> {
			file.select(true);
		});
		TorrentStreamGroup group = TorrentStreamGroup.newInstance("e://tmp/client/", session.torrent(), files, session);
//		TorrentStreamGroup group = TorrentStreamGroup.newInstance("E:\\tmp\\[Skytree][海贼王][One_Piece][884][GB_JP][X264_AAC][720P][CRRIP][天空树双语字幕组].mp4", session.torrent(), files, session);
		var downloadPieces = group.pieces();
		int index = downloadPieces.nextSetBit(0);
		int length = session.torrent().getInfo().getPieceLength().intValue();
		System.out.println(downloadPieces.cardinality());
		System.out.println("总长度：" + (pieces.length / 20));
		while(downloadPieces.nextSetBit(index) >= 0) {
			var piece = group.read(index, 0, length);
			if(piece == null) {
				System.out.println("序号：" + index + "->null");
				index++;
				continue;
			}
			if(!ArrayUtils.equals(StringUtils.sha1(piece), select(pieces, index))) {
//			if(!StringUtils.sha1Hex(piece).equals(StringUtils.hex(select(pieces, index)))) {
				System.out.println("序号：" + index + "->" + StringUtils.sha1Hex(piece) + "=" + StringUtils.hex(select(pieces, index)));
			}
			index++;
		}
		System.out.println(downloadPieces);
	}

	private byte[] select(byte[] pieces, int index) {
		byte[] value = new byte[20];
		System.arraycopy(pieces, index * 20, value, 0, 20);
		return value;
	}
	
}
