package com.acgist.snail.net.torrent.tracker.bootstrap;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.utils.Performance;

public class HttpTrackerSessionTest extends Performance {

	@Test
	public void testAnnounce() throws DownloadException, NetException {
		final String path = "E:/snail/12345.torrent";
		final TorrentSession torrentSession = TorrentManager.getInstance().newTorrentSession(path);
//		final HttpTrackerSession session = HttpTrackerSession.newInstance("http://www.proxmox.com:6969/announce");
		final HttpTrackerSession session = HttpTrackerSession.newInstance("http://tracker3.itzmx.com:6961/announce");
//		final HttpTrackerSession session = HttpTrackerSession.newInstance("http://opentracker.acgnx.se/announce");
		session.announce(1000, torrentSession);
//		session.scrape(1000, session);
	}
	
}
