package com.acgist.snail.tracker;

import org.junit.Test;

import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerClient;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerManager;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.ThreadUtils;

public class TrackerClientUdpTest {

	@Test
	public void announce() throws NetException, DownloadException {
		String path = "e:/snail/12345.torrent";
		TorrentSession session = TorrentManager.getInstance().newTorrentSession(path);
//		UdpTrackerClient client = UdpTrackerClient.newInstance("udp://exodus.desync.com:6969/announce");
//		TrackerManager.getInstance().newTrackerLauncher(client, session);
		var list = TrackerManager.getInstance().clients("udp://explodie.org:6969/announce", null);
		TrackerClient client = list.get(0);
		client.announce(1000, session);
		client.scrape(1000, session);
		ThreadUtils.sleep(Long.MAX_VALUE);
	}
	
}
