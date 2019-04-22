package com.acgist.main;

import org.junit.Test;

import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.torrent.TorrentBuilder;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.manager.TorrentSessionManager;

public class TorrentBuilderTest {

	@Test
	public void test() throws DownloadException {
		String path = "e:/snail/12345.torrent";
		TorrentSession session = TorrentSessionManager.getInstance().buildSession(path);
		InfoHash infoHash = session.infoHash();
		TorrentBuilder builder = TorrentBuilder.newInstance(infoHash);
		builder.buildFile("e:/snail");
	}
	
}
