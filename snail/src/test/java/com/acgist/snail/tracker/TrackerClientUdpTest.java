package com.acgist.snail.tracker;

import org.junit.jupiter.api.Test;

import com.acgist.snail.BaseTest;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerClient;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerManager;
import com.acgist.snail.pojo.session.TorrentSession;

public class TrackerClientUdpTest extends BaseTest {

	@Test
	public void testAnnounce() throws NetException, DownloadException {
		String path = "e:/snail/12345.torrent";
		TorrentSession session = TorrentManager.getInstance().newTorrentSession(path);
//		var list = TrackerManager.getInstance().clients("udp://explodie.org:6969/announce", null);
		var list = TrackerManager.getInstance().clients("udp://retracker.akado-ural.ru/announce", null);
		TrackerClient client = list.get(0);
		client.announce(1000, session);
		client.scrape(1000, session);
		this.pause();
	}
	
}
