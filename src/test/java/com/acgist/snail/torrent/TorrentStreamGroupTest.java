package com.acgist.snail.torrent;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.junit.Test;

import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.bootstrap.TorrentStreamGroup;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.ThreadUtils;

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
		TorrentStreamGroup group = TorrentStreamGroup.newInstance("e://tmp//test", files, session);
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
	public void verify() throws DownloadException, NetException {
//		String path = "e:/snail/12345.torrent";
		String path = "E:\\gitee\\snail\\download\\[UHA-WINGS][Sewayaki Kitsune no Senko-san][07][x264 1080p][CHT].mp4\\【悠哈璃羽字幕社】[賢惠幼妻仙狐小姐_Sewayaki Kitsune no Senko-san][07][x264 1080p][CHT].torrent";
		TorrentSession session = TorrentManager.getInstance().newTorrentSession(path);
		var files = session.torrent().getInfo().files();
		files.forEach(file -> {
			file.select(true);
		});
//		TorrentStreamGroup group = TorrentStreamGroup.newInstance("e://tmp/client/", files, session);
		TorrentStreamGroup group = TorrentStreamGroup.newInstance("E:\\gitee\\snail\\download\\[UHA-WINGS][Sewayaki Kitsune no Senko-san][07][x264 1080p][CHT].mp4", files, session);
		ThreadUtils.sleep(5000); // 等待异步加载完成
		var downloadPieces = group.pieces();
		int index = downloadPieces.nextSetBit(0);
		int length = session.torrent().getInfo().getPieceLength().intValue();
		System.out.println(downloadPieces.cardinality());
		System.out.println("总长度：" + session.torrent().getInfo().pieceSize());
		long begin = System.currentTimeMillis();
		while(downloadPieces.nextSetBit(index) >= 0) {
			var piece = group.read(index, 0, length);
			if(piece == null) {
				System.out.println("序号：" + index + "->null");
				index++;
				continue;
			}
			if(!ArrayUtils.equals(StringUtils.sha1(piece), group.pieceHash(index))) {
//			if(!StringUtils.sha1Hex(piece).equals(StringUtils.hex(select(pieces, index)))) {
				System.out.println("序号：" + index + "->" + StringUtils.sha1Hex(piece) + "=" + StringUtils.hex(group.pieceHash(index)));
			}
			index++;
		}
		System.out.println("校验时间：" + (System.currentTimeMillis() - begin));
		System.out.println(downloadPieces);
		System.out.println(group.selectPieces());
	}

	@Test
	public void compare() throws IOException {
		int index = 22;
		int pieceSize = 524288;
		String fileA = "E:\\gitee\\snail\\download\\[Sakurato.sub][One Punch Man 2nd Season][06][GB][720P]\\[Sakurato.sub][One Punch Man 2nd Season][06][GB][720P].mp4";
		String fileB = "E:\\gitee\\snail\\download\\[Sakurato.sub][One Punch Man 2nd Season][06][GB][720P]\\[Sakurato.sub][One Punch Man 2nd Season][06][GB][720P]s.mp4";
		RandomAccessFile streamA = new RandomAccessFile(fileA, "rw");
		RandomAccessFile streamB = new RandomAccessFile(fileB, "rw");
		byte[] byteA = new byte[pieceSize];
		byte[] byteB = new byte[pieceSize];
		streamA.seek(index * pieceSize);
		streamA.read(byteA);
		streamB.seek(index * pieceSize);
		streamB.read(byteB);
		System.out.println(ArrayUtils.equals(byteA, byteB));
		streamA.close();
		streamB.close();
	}
	
}
