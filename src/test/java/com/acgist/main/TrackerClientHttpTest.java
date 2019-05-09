package com.acgist.main;

import org.junit.Test;

import com.acgist.snail.net.tracker.bootstrap.impl.HttpTrackerClient;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.system.manager.TorrentManager;

public class TrackerClientHttpTest {

	@Test
	public void test() throws DownloadException, NetException {
		String path = "e:/snail/82309348090ecbec8bf509b83b30b78a8d1f6454.torrent";
		TorrentSession session = TorrentManager.getInstance().newTorrentSession(path);
//		HttpTrackerClient client = HttpTrackerClient.newInstance("http://anidex.moe:6969/announce");
		HttpTrackerClient client = HttpTrackerClient.newInstance("http://t.nyaatracker.com/announce");
		client.announce(1000, session);
	}
	
}
