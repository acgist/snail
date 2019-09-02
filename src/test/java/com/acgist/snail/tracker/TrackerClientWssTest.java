package com.acgist.snail.tracker;

import org.junit.Test;

import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.tracker.bootstrap.impl.WssTrackerClient;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.ThreadUtils;

@SuppressWarnings("deprecation")
public class TrackerClientWssTest {
	
	@Test
	public void test() throws NetException, DownloadException {
		String path = "e:/snail/sintel.torrent";
		TorrentSession session = TorrentManager.getInstance().newTorrentSession(path);
//		WssTrackerClient client = WssTrackerClient.newInstance("wss://tracker.fastcast.nz");
//		WssTrackerClient client = WssTrackerClient.newInstance("wss://tracker.btorrent.xyz");
		WssTrackerClient client = WssTrackerClient.newInstance("wss://tracker.openwebtorrent.com");
		while (true) {
			client.announce(1000, session);
			ThreadUtils.sleep(5000);
		}
	}
	
}
