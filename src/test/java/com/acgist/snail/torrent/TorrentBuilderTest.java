package com.acgist.snail.torrent;

import java.util.List;

import org.junit.Test;

import com.acgist.snail.BaseTest;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.dht.bootstrap.NodeManager;
import com.acgist.snail.pojo.bean.InfoHash;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.magnet.bootstrap.TorrentBuilder;
import com.acgist.snail.system.exception.DownloadException;

public class TorrentBuilderTest extends BaseTest {

	@Test
	public void build() throws DownloadException {
		String path = "e:/snail/12345.torrent";
		TorrentSession session = TorrentManager.getInstance().newTorrentSession(path);
		InfoHash infoHash = session.infoHash();
		NodeManager.getInstance().newNodeSession("12345678901234567890".getBytes(), "192.168.1.1", 18888);
		var trackers = List.of("https://www.acgist.com", "https://www.acgist.com/1", "https://www.acgist.com/2");
		TorrentBuilder builder = TorrentBuilder.newInstance(infoHash, trackers);
		builder.buildFile("e:/tmp/torrent");
	}
	
}
