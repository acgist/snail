package com.acgist.main;

import org.junit.Test;

import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.context.SystemContext;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.system.manager.TorrentSessionManager;
import com.acgist.snail.system.manager.TrackerClientManager;
import com.acgist.snail.system.manager.TrackerLauncherManager;
import com.acgist.snail.utils.ThreadUtils;

public class TrackerClientUdpTest {

	@Test
	public void test() throws NetException, DownloadException {
		SystemContext.init();
		String path = "e:/snail/82309348090ecbec8bf509b83b30b78a8d1f6454.torrent";
		TorrentSession session = TorrentSessionManager.getInstance().buildSession(path);
		var client = TrackerClientManager.getInstance().register("udp://exodus.desync.com:6969/announce");
		var launcher = TrackerLauncherManager.getInstance().build(client, session);
		client.announce(launcher.id(), session);
		client = TrackerClientManager.getInstance().register("udp://tracker.uw0.xyz:6969/announce");
		launcher = TrackerLauncherManager.getInstance().build(client, session);
		client.announce(launcher.id(), session);
		ThreadUtils.sleep(1000000);
	}

}
