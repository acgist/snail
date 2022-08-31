package com.acgist.snail.pojo.bean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.TorrentContext;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.pojo.Torrent;
import com.acgist.snail.pojo.TorrentFile;
import com.acgist.snail.pojo.TorrentInfo;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.utils.DateUtils;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.StringUtils;

class TorrentTest extends Performance {

	@Test
	void testTorrent() throws Exception {
//		String path = "D:/tmp/snail/07E1B909D8D193D80E440A8593FB57A658223A0E.torrent"; // 没有编码：GBK
//		String path = "D:/tmp/snail/b3e9dcb123b80078aa5ace79323f925e8f755a6a.torrent"; // 没有编码：UTF-8
		String path = "D:/tmp/snail/902FFAA29EE632C8DC966ED9AB573409BA9A518E.torrent";
//		String path = "D:/tmp/snail/0B156834B59B0FF64EE0C9305D4D6EDE421196E6.torrent"; // 填充文件
//		String path = "D:/tmp/snail/8443b6eebb1dc6c50f9c1621b059832e6556242a.torrent"; // 巨大文件
		final TorrentSession session = TorrentContext.getInstance().newTorrentSession(path);
		final Torrent torrent = session.torrent();
		final TorrentInfo torrentInfo = torrent.getInfo();
		assertEquals(path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.')).toUpperCase(), session.infoHash().infoHashHex().toUpperCase());
		this.log("注释：" + torrent.getComment());
		this.log("Piece数量：" + torrent.getInfo().pieceSize());
		this.log("Piece长度：" + torrent.getInfo().getPieceLength());
		this.log("编码格式：" + torrent.getEncoding());
		if(torrent.getCreationDate() != null) {
			this.log("创建时间：" + DateUtils.unixToJavaDate(torrent.getCreationDate()));
		}
		this.log("创建者：" + torrent.getCreatedBy());
		this.log("私有种子：" + torrentInfo.getPrivateTorrent());
		this.log("Tracker：" + torrent.getAnnounce());
		this.log("Trackers：" + torrent.getAnnounceList());
		this.log("DHT节点：" + torrent.getNodes());
		for (TorrentFile file : torrentInfo.files()) {
			this.log("填充文件：" + file.paddingFile());
			this.log("文件大小：" + file.getLength());
			this.log("文件ed2k：" + StringUtils.hex(file.getEd2k()));
			this.log("文件Hash：" + StringUtils.hex(file.getFilehash()));
			this.log("文件路径：" + String.join("/", file.getPath()));
			this.log("文件路径UTF-8：" + String.join("/", file.getPathUtf8()));
		}
	}
	
	@Test
	void testCosted() throws DownloadException {
		assertDoesNotThrow(() -> this.costed(1000, () -> {
			try {
				TorrentContext.loadTorrent("E:/snail/b3e9dcb123b80078aa5ace79323f925e8f755a6a.torrent");
			} catch (DownloadException e) {
				this.log("加载种子异常", e);
			}
		}));
	}
	
}
