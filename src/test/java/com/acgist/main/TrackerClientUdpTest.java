package com.acgist.main;

import org.junit.Test;

import com.acgist.snail.downloader.torrent.bootstrap.TrackerLauncherGroup;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.session.TaskSession;
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
		TrackerLauncherGroup group = TrackerLauncherGroup.newInstance(session);
		TaskEntity entity = new TaskEntity();
		entity.setFile("e://tmp/test");
		entity.setSize(100L);
		session.loadTask(TaskSession.newInstance(entity));
		group.loadTracker();
		ThreadUtils.sleep(Long.MAX_VALUE);
	}

}
