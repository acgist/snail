package com.acgist.snail.tracker;

import org.junit.Test;

import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.tracker.bootstrap.impl.WsTrackerClient;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.ThreadUtils;

public class TrackerClientWsTest {
	
	@Test
	public void test() throws NetException, DownloadException {
		String path = "e:/snail/12345.torrent";
		TorrentSession session = TorrentManager.getInstance().newTorrentSession(path);
//		WsTrackerClient client = WsTrackerClient.newInstance("wss://tracker.fastcast.nz");
//		WsTrackerClient client = WsTrackerClient.newInstance("wss://tracker.btorrent.xyz");
		WsTrackerClient client = WsTrackerClient.newInstance("wss://tracker.openwebtorrent.com");
		while (true) {
			client.announce(1000, session);
			ThreadUtils.sleep(5000);
		}
	}
	
}
