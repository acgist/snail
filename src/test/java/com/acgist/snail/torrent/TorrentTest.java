package com.acgist.snail.torrent;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.torrent.bean.Torrent;
import com.acgist.snail.protocol.torrent.bean.TorrentInfo;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.DateUtils;

public class TorrentTest {

	@Test
	public void read() throws Exception {
//		String path = "e:/snail/12345.torrent";
//		String path = "e:/snail/5b293c290c78c503bcd59bc0fbf78fd213ce21a4.torrent";
//		String path = "e:/snail/9d3854d8c6280049e5d85e490ff07de7c2bd96a2.torrent";
//		String path = "e:/snail/868f1199b18d05bf103aa8a8321f6428854d712e.torrent";
//		String path = "e:/snail/11e38a5270e15c60534ca48977b7d77a3c4f6340.torrent";
		String path = "e:/snail/0B156834B59B0FF64EE0C9305D4D6EDE421196E6.torrent";
//		String path = "e:/snail/543/c15417e6aeab33732a59085d826edd29978f9afa.torrent";
		TorrentSession session = TorrentManager.getInstance().newTorrentSession(path);
		Torrent torrent = session.torrent();
		System.out.println(torrent.getComment());
		System.out.println(torrent.getInfo().pieceSize());
		System.out.println("种子HASH：" + session.infoHash().infoHashHex());
		System.out.println(DateUtils.unixToJavaDate(torrent.getCreationDate()));
		System.out.println(torrent.getCreationDate());
		System.out.println(torrent.getEncoding());
		System.out.println(torrent.getCreatedBy());
		System.out.println(torrent.getInfo().getLength());
		System.out.println(torrent.getInfo().getPieceLength());
		System.out.println(torrent.getInfo().getPieces().length);
		System.out.println(torrent.getAnnounce());
		if (torrent.getAnnounceList().size() > 0) {
			for (String tmp : torrent.getAnnounceList()) {
				System.out.println(tmp);
			}
		}
		System.out.println(torrent.getNodes());
		TorrentInfo torrentInfo = torrent.getInfo();
		System.out.println(torrentInfo.getName());
		System.out.println(torrentInfo.getLength());
		System.out.println(torrentInfo.getPieceLength());
		System.out.println(torrentInfo.getPieces());
		System.out.println(torrentInfo.ed2kHex());
		System.out.println(torrentInfo.filehashHex());
//		if (torrentInfo.getFiles().size() > 0) {
//			for (TorrentFile file : torrentInfo.getFiles()) {
//				System.out.println("----------------file----------------");
//				System.out.println("文件长度：" + file.getLength());
//				System.out.println("ed2k：" + StringUtils.hex(file.getEd2k()));
//				System.out.println("filehash：" + StringUtils.hex(file.getFilehash()));
//				if (file.getPath().size() > 0) {
//					System.out.println("文件路径：" + String.join("/", file.getPath()));
//					System.out.println("文件路径UTF-8：" + String.join("/", file.getPathUtf8()));
//				}
//			}
//		}
//		System.out.println(JsonUtils.toJson(torrent));
	}
	
	@Test
	public void time() throws DownloadException {
		long begin = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			TorrentManager.loadTorrent("e://fa493c8add6d907a0575631831033dcf94ba5217.torrent");
		}
		long end = System.currentTimeMillis();
		System.out.println(end - begin);
	}
	
	@Test
	public void cos() {
		List<?> list = List.of("1", "2", "2", "2", "2", "2", "2");
		List<Object> objects = null;
		long begin = System.currentTimeMillis();
		for (int index = 0; index < 100000; index++) {
			objects = list.stream()
				.map(value -> value)
				.collect(Collectors.toList());
		}
		System.out.println(System.currentTimeMillis() - begin);
		System.out.println(objects);
	}
	
}
