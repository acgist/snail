package com.acgist.main;

import java.util.Date;

import org.junit.Test;

import com.acgist.snail.coder.torrent.TorrentDecoder;
import com.acgist.snail.coder.torrent.TorrentInfo;
import com.acgist.snail.utils.JSONUtils;

public class TorrentDecoderTest {

	@Test
	public void test() throws Exception {
		String path = "e:/snail/5b293c290c78c503bcd59bc0fbf78fd213ce21a4.torrent";
//		String path = "e:/snail/82309348090ecbec8bf509b83b30b78a8d1f6454.torrent";
//		String path = "e:/snail/9d3854d8c6280049e5d85e490ff07de7c2bd96a2.torrent";
//		String path = "e:/snail/0B156834B59B0FF64EE0C9305D4D6EDE421196E6.torrent";
		TorrentDecoder decoder = TorrentDecoder.newInstance(path);
		TorrentInfo info = decoder.torrentWrapper().torrentInfo();
		System.out.println(decoder.hash());
		System.out.println(new Date(info.getCreationDate() * 1000));
//		System.out.println(info.getCreationDate());
//		System.out.println(info.getEncoding());
//		System.out.println(info.getCreateBy());
//		System.out.println(info.getInfo().getLength());
//		System.out.println(info.getAnnounce());
//		if (info.getAnnounceList().size() > 0) {
//			for (String tmp : info.getAnnounceList()) {
//				System.out.println(tmp);
//			}
//		}
//		TorrentFiles files = info.getInfo();
//		System.out.println(files.getName());
//		System.out.println(files.getLength());
//		System.out.println(files.getPieceLength());
//		System.out.println(files.getPieces());
//		System.out.println(files.getEd2kHex());
//		System.out.println(files.getFilehashHex());
//		if (files.getFiles().size() > 0) {
//			for (TorrentFile file : files.getFiles()) {
//				System.out.println("----------------file----------------");
//				System.out.println(file.getLength());
//				System.out.println(StringUtils.hex(file.getEd2k()));
//				System.out.println(StringUtils.hex(file.getFilehash()));
//				System.out.println("--------path--------");
//				if (file.getPath().size() > 0) {
//					for (String tmp : file.getPath()) {
//						System.out.println(tmp);
//					}
//				}
//			}
//		}
		System.out.println(JSONUtils.javaToJson(info));
	}
	
}
