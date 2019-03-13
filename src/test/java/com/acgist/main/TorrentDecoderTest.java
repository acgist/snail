package com.acgist.main;

import org.junit.Test;

import com.acgist.snail.coder.torrent.TorrentDecoder;
import com.acgist.snail.coder.torrent.pojo.Torrent;

public class TorrentDecoderTest {

	@Test
	public void test() throws Exception {
//		String path = "e:/snail/5b293c290c78c503bcd59bc0fbf78fd213ce21a4.torrent";
//		String path = "e:/snail/82309348090ecbec8bf509b83b30b78a8d1f6454.torrent";
//		String path = "e:/snail/9d3854d8c6280049e5d85e490ff07de7c2bd96a2.torrent";
		String path = "e:/snail/868f1199b18d05bf103aa8a8321f6428854d712e.torrent";
//		String path = "e:/snail/0B156834B59B0FF64EE0C9305D4D6EDE421196E6.torrent";
		TorrentDecoder decoder = TorrentDecoder.newInstance(path);
		Torrent torrent = decoder.torrentWrapper().torrent();
		System.out.println(decoder.hash());
		System.out.println(decoder.infoHash());
//		System.out.println(new Date(torrent.getCreationDate() * 1000));
//		System.out.println(torrent.getCreationDate());
//		System.out.println(torrent.getEncoding());
//		System.out.println(torrent.getCreateBy());
//		System.out.println(torrent.getInfo().getLength());
		System.out.println(torrent.getAnnounce());
		if (torrent.getAnnounceList().size() > 0) {
			for (String tmp : torrent.getAnnounceList()) {
				System.out.println(tmp);
			}
		}
//		TorrentInfo torrentInfo = torrent.getInfo();
//		System.out.println(torrentInfo.getName());
//		System.out.println(torrentInfo.getLength());
//		System.out.println(torrentInfo.getPieceLength());
//		System.out.println(torrentInfo.getPieces());
//		System.out.println(torrentInfo.getEd2kHex());
//		System.out.println(torrentInfo.getFilehashHex());
//		if (torrentInfo.getFiles().size() > 0) {
//			for (TorrentFile file : torrentInfo.getFiles()) {
//				System.out.println("----------------file----------------");
//				System.out.println("文件长度：" + file.getLength());
//				System.out.println("ed2k：" + StringUtils.hex(file.getEd2k()));
//				System.out.println("filehash：" + StringUtils.hex(file.getFilehash()));
//				System.out.println("文件路径：");
//				if (file.getPath().size() > 0) {
//					for (String tmp : file.getPath()) {
//						System.out.print(tmp + " / ");
//					}
//				}
//				System.out.println();
//			}
//		}
//		System.out.println(JsonUtils.toJson(torrent));
	}
	
}
