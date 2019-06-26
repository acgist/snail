package com.acgist.snail.tracker;

import org.junit.Test;

import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.tracker.bootstrap.impl.WssTrackerClient;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.ThreadUtils;

public class TrackerClientWssTest {

	@Test
	public void test() throws NetException, DownloadException {
		String path = "e:/snail/16b1233b33143700fe47910898fcaaf0f05d2d09.torrent";
		TorrentSession session = TorrentManager.getInstance().newTorrentSession(path);
		WssTrackerClient client = WssTrackerClient.newInstance("wss://tracker.fastcast.nz/announce");
		client.announce(1000, session);
		ThreadUtils.sleep(Long.MAX_VALUE);
	}
	
}
