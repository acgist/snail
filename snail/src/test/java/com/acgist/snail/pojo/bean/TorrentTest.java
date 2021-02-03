package com.acgist.snail.pojo.bean;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.TorrentContext;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.utils.DateUtils;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.StringUtils;

public class TorrentTest extends Performance {

	@Test
	public void testRead() throws Exception {
//		String path = "e:/snail/07E1B909D8D193D80E440A8593FB57A658223A0E.torrent"; // 没有编码：GBK
//		String path = "e:/snail/b3e9dcb123b80078aa5ace79323f925e8f755a6a.torrent"; // 没有编码：UTF-8
//		String path = "e:/snail/902FFAA29EE632C8DC966ED9AB573409BA9A518E.torrent";
//		String path = "e:/snail/0B156834B59B0FF64EE0C9305D4D6EDE421196E6.torrent"; // 填充文件
		String path = "e:/snail/8443b6eebb1dc6c50f9c1621b059832e6556242a.torrent"; // 巨大文件
		TorrentSession session = TorrentContext.getInstance().newTorrentSession(path);
		Torrent torrent = session.torrent();
		this.log("注释：" + torrent.getComment());
		this.log("Piece数量：" + torrent.getInfo().pieceSize());
		this.log("Piece长度：" + torrent.getInfo().getPieceLength());
		this.log("Piece Hash长度：" + torrent.getInfo().getPieces().length);
		this.log("种子Hash：" + session.infoHash().infoHashHex());
		if(torrent.getCreationDate() != null) {
			this.log("创建时间：" + DateUtils.unixToJavaDate(torrent.getCreationDate()));
		}
		this.log("创建时间：" + torrent.getCreationDate());
		this.log("编码格式：" + torrent.getEncoding());
		this.log("创建者：" + torrent.getCreatedBy());
		this.log("私有种子：" + torrent.getInfo().getPrivateTorrent());
		this.log("Tracker：" + torrent.getAnnounce());
		if (torrent.getAnnounceList().size() > 0) {
			for (String tmp : torrent.getAnnounceList()) {
				this.log("Tracker：" + tmp);
			}
		}
		this.log("DHT节点：" + torrent.getNodes());
		TorrentInfo torrentInfo = torrent.getInfo();
		// 单文件
		this.log("文件名称：" + torrentInfo.getName());
		this.log("文件名称UTF-8：" + torrentInfo.getNameUtf8());
		this.log("文件大小：" + torrentInfo.getLength());
		this.log("文件ED2K：" + StringUtils.hex(torrentInfo.getEd2k()));
		this.log("文件Hash：" + StringUtils.hex(torrentInfo.getFilehash()));
		// 多文件
		if (torrentInfo.getFiles().size() > 0) {
			for (TorrentFile file : torrentInfo.getFiles()) {
				this.log("填充文件：" + file.isPaddingFile());
				this.log("文件大小：" + file.getLength());
				this.log("文件ED2K：" + StringUtils.hex(file.getEd2k()));
				this.log("文件Hash：" + StringUtils.hex(file.getFilehash()));
				if (file.getPath().size() > 0) {
					this.log("文件路径：" + String.join("/", file.getPath()));
					this.log("文件路径UTF-8：" + String.join("/", file.getPathUtf8()));
				}
			}
		}
	}
	
	@Test
	public void testCos() throws DownloadException {
		this.cost();
		for (int i = 0; i < 10000; i++) {
			TorrentContext.loadTorrent("e:/snail/868f1199b18d05bf103aa8a8321f6428854d712e.torrent");
		}
		this.costed();
	}
	
}
