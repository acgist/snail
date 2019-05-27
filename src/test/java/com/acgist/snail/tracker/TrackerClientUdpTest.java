package com.acgist.snail.tracker;

import org.junit.Test;

import com.acgist.snail.downloader.torrent.bootstrap.TrackerLauncherGroup;
import com.acgist.snail.net.tracker.bootstrap.TrackerClient;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.system.manager.TorrentManager;
import com.acgist.snail.system.manager.TrackerManager;
import com.acgist.snail.utils.ThreadUtils;

public class TrackerClientUdpTest {

	@Test
	public void test() throws NetException, DownloadException {
		String path = "e:/snail/1234.torrent";
		TorrentSession session = TorrentManager.getInstance().newTorrentSession(path);
		TrackerLauncherGroup group = TrackerLauncherGroup.newInstance(session);
		TaskEntity entity = new TaskEntity();
		entity.setFile("e://tmp/test");
		entity.setSize(100L);
		session.upload(TaskSession.newInstance(entity)).download(false);
		group.loadTracker();
		ThreadUtils.sleep(Long.MAX_VALUE);
	}

	@Test
	public void udp() throws NetException, DownloadException {
		String path = "e:/snail/82309348090ecbec8bf509b83b30b78a8d1f6454.torrent";
		TorrentSession session = TorrentManager.getInstance().newTorrentSession(path);
//		UdpTrackerClient client = UdpTrackerClient.newInstance("udp://exodus.desync.com:6969/announce");
//		TrackerManager.getInstance().newTrackerLauncher(client, session);
		var list = TrackerManager.getInstance().clients("udp://exodus.desync.com:6969/announce", null);
		TrackerClient client = list.get(0);
		client.announce(1000, session);
		ThreadUtils.sleep(Long.MAX_VALUE);
	}
	
}
