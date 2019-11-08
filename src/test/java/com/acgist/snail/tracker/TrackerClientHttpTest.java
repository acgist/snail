package com.acgist.snail.tracker;

import org.junit.Test;

import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.tracker.bootstrap.impl.HttpTrackerClient;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.exception.NetException;

public class TrackerClientHttpTest {

	@Test
	public void announce() throws DownloadException, NetException {
		String path = "e:/snail/12345.torrent";
		TorrentSession session = TorrentManager.getInstance().newTorrentSession(path);
		HttpTrackerClient client = HttpTrackerClient.newInstance("http://tracker3.itzmx.com:6961/announce");
//		HttpTrackerClient client = HttpTrackerClient.newInstance("http://opentracker.acgnx.se/announce");
		client.announce(1000, session);
		client.scrape(1000, session);
	}
	
}
