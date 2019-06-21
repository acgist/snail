package com.acgist.snail.torrent;

import org.junit.Test;

import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.magnet.bootstrap.TorrentBuilder;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.manager.TorrentManager;

public class TorrentBuilderTest {

	@Test
	public void test() throws DownloadException {
		String path = "e:/snail/12345.torrent";
		TorrentSession session = TorrentManager.getInstance().newTorrentSession(path);
		InfoHash infoHash = session.infoHash();
		TorrentBuilder builder = TorrentBuilder.newInstance(infoHash, null);
		builder.buildFile("e:/snail");
	}
	
}
