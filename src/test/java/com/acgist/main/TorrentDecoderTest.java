package com.acgist.main;

import java.util.Date;

import org.junit.Test;

import com.acgist.snail.module.coder.torrent.TorrentDecoder;
import com.acgist.snail.module.coder.torrent.TorrentFile;
import com.acgist.snail.module.coder.torrent.TorrentFiles;
import com.acgist.snail.module.coder.torrent.TorrentInfo;
import com.acgist.snail.utils.JSONUtils;
import com.acgist.snail.utils.StringUtils;

public class TorrentDecoderTest {

	@Test
	public void test() throws Exception {
//		TorrentInfo info = TorrentDecoder.decode("e:/snail/82309348090ecbec8bf509b83b30b78a8d1f6454.torrent");
//		TorrentInfo info = TorrentDecoder.decode("e:/snail/9d3854d8c6280049e5d85e490ff07de7c2bd96a2.torrent"); // 单文件
		TorrentInfo info = TorrentDecoder.decode("e:/snail/0B156834B59B0FF64EE0C9305D4D6EDE421196E6.torrent");
		System.out.println(new Date(info.getCreationDate() * 1000));
		System.out.println(info.getCreationDate());
		System.out.println(info.getEncoding());
		System.out.println(info.getCreateBy());
		System.out.println(info.getInfo().getLength());
//		if (info.getAnnounceList().size() > 0) {
//			for (String tmp : info.getAnnounceList()) {
//				System.out.println(tmp);
//			}
//		}
		TorrentFiles files = info.getInfo();
		System.out.println(files.getName());
		System.out.println(files.getLength());
		System.out.println(files.getPieceLength());
		System.out.println(files.getPieces());
		System.out.println(StringUtils.hex(files.getEd2k()));
		System.out.println(StringUtils.hex(files.getFilehash()));
		if (files.getFiles().size() > 0) {
			for (TorrentFile file : files.getFiles()) {
				System.out.println("--------file--------");
				System.out.println(file.getLength());
				System.out.println(StringUtils.hex(file.getEd2k()));
				System.out.println(StringUtils.hex(file.getFilehash()));
				System.out.println("--------path--------");
				if (file.getPath().size() > 0) {
					for (String tmp : file.getPath()) {
						System.out.println(tmp);
					}
				}
			}
		}
		System.out.println(JSONUtils.javaToJson(info));
	}
	
}
