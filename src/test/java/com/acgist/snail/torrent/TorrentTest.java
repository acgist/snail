package com.acgist.snail.torrent;

import org.junit.Test;

import com.acgist.snail.BaseTest;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.pojo.bean.Torrent;
import com.acgist.snail.pojo.bean.TorrentFile;
import com.acgist.snail.pojo.bean.TorrentInfo;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.DateUtils;
import com.acgist.snail.utils.StringUtils;

public class TorrentTest extends BaseTest {

	@Test
	public void read() throws Exception {
//		String path = "e:/snail/hide.torrent"; // 私有种子：影身种子
//		String path = "e:/snail/12345.torrent";
//		String path = "e:/snail/5b293c290c78c503bcd59bc0fbf78fd213ce21a4.torrent";
//		String path = "e:/snail/9d3854d8c6280049e5d85e490ff07de7c2bd96a2.torrent";
//		String path = "e:/snail/868f1199b18d05bf103aa8a8321f6428854d712e.torrent";
//		String path = "e:/snail/11e38a5270e15c60534ca48977b7d77a3c4f6340.torrent";
		String path = "e:/snail/0B156834B59B0FF64EE0C9305D4D6EDE421196E6.torrent";
//		String path = "e:/snail/543/c15417e6aeab33732a59085d826edd29978f9afa.torrent";
//		String path = "e:/snail/641000d9be79ad8947701c338c06211ba69e1b09.torrent";
		TorrentSession session = TorrentManager.getInstance().newTorrentSession(path);
		Torrent torrent = session.torrent();
		this.log(torrent.getComment());
		this.log(torrent.getInfo().pieceSize());
		this.log("种子HASH：" + session.infoHash().infoHashHex());
		this.log(DateUtils.unixToJavaDate(torrent.getCreationDate()));
		this.log(torrent.getCreationDate());
		this.log(torrent.getEncoding());
		this.log(torrent.getCreatedBy());
		this.log(torrent.getInfo().getLength());
		this.log(torrent.getInfo().getPieceLength());
		this.log(torrent.getInfo().getPieces().length);
		this.log(torrent.getAnnounce());
		this.log("私有种子：" + torrent.getInfo().getPrivateTorrent());
		if (torrent.getAnnounceList().size() > 0) {
			for (String tmp : torrent.getAnnounceList()) {
				this.log(tmp);
			}
		}
		this.log(torrent.getNodes());
		TorrentInfo torrentInfo = torrent.getInfo();
		this.log(torrentInfo.getName());
		this.log(torrentInfo.getLength());
		this.log(torrentInfo.getPieceLength());
		this.log(torrentInfo.getPieces());
		this.log(torrentInfo.getEd2k());
		this.log(torrentInfo.getFilehash());
		if (torrentInfo.getFiles().size() > 0) {
			for (TorrentFile file : torrentInfo.getFiles()) {
				this.log("----------------file----------------");
				this.log("文件长度：" + file.getLength());
				this.log("ed2k：" + StringUtils.hex(file.getEd2k()));
				this.log("filehash：" + StringUtils.hex(file.getFilehash()));
				if (file.getPath().size() > 0) {
					this.log("文件路径：" + String.join("/", file.getPath()));
					this.log("文件路径UTF-8：" + String.join("/", file.getPathUtf8()));
				}
			}
		}
	}
	
	@Test
	public void time() throws DownloadException {
		long begin = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			TorrentManager.loadTorrent("e:/fa493c8add6d907a0575631831033dcf94ba5217.torrent");
		}
		long end = System.currentTimeMillis();
		this.log(end - begin);
	}
	
}
