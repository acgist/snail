package com.acgist.snail.net.torrent.tracker;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.pojo.InfoHash;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.ThreadUtils;

class TrackerLauncherGroupTest extends Performance {

	@Test
	void testRelease() throws DownloadException, InterruptedException {
		final var entity = new TaskEntity();
		entity.setUrl("1".repeat(40));
		entity.setSize(0L);
		final var session = TorrentSession.newInstance(InfoHash.newInstance("1".repeat(40)), null);
		session.magnet(TaskSession.newInstance(entity));
		final var group = TrackerLauncherGroup.newInstance(session);
		group.loadTracker();
		final var thread = new Thread(() -> group.findPeer());
		thread.start();
		ThreadUtils.sleep(1000);
		group.release();
		final long start = System.currentTimeMillis();
		thread.join();
		final long end = System.currentTimeMillis();
		assertTrue(SystemConfig.CONNECT_TIMEOUT_MILLIS + 1000 > end - start);
	}
	
}
