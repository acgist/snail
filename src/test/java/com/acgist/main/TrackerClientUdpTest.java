package com.acgist.main;

import org.junit.Test;

import com.acgist.snail.net.tracker.TrackerGroup;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.system.manager.TorrentSessionManager;
import com.acgist.snail.utils.ThreadUtils;

public class TrackerClientUdpTest {

	@Test
	public void test() throws NetException, DownloadException {
		String path = "e:/snail/1234.torrent";
		TorrentSession session = TorrentSessionManager.getInstance().buildSession(path);
		TrackerGroup group = new TrackerGroup(session);
		group.loadTracker();
//		var client = TrackerClientManager.getInstance().register("udp://exodus.desync.com:6969/announce");
//		var launcher = TrackerLauncherManager.getInstance().build(client, session);
//		client.announce(launcher.id(), session);
//		client = TrackerClientManager.getInstance().register("udp://tracker.uw0.xyz:6969/announce");
//		launcher = TrackerLauncherManager.getInstance().build(client, session);
//		client.announce(launcher.id(), session);
		ThreadUtils.sleep(Long.MAX_VALUE);
	}

}
