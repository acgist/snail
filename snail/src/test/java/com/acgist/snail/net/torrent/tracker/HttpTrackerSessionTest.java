package com.acgist.snail.net.torrent.tracker;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.DownloadException;
import com.acgist.snail.net.NetException;
import com.acgist.snail.net.torrent.TorrentContext;
import com.acgist.snail.net.torrent.TorrentSession;
import com.acgist.snail.utils.Performance;

class HttpTrackerSessionTest extends Performance {

	@Test
	void testAnnounce() throws DownloadException, NetException {
		final String path = "D:/tmp/snail/902FFAA29EE632C8DC966ED9AB573409BA9A518E.torrent";
		final TorrentSession torrentSession = TorrentContext.getInstance().newTorrentSession(path);
		final HttpTrackerSession session = HttpTrackerSession.newInstance("http://vps02.net.orel.ru:80/announce");
		session.started(1000, torrentSession);
		session.scrape(1000, torrentSession);
		assertNotNull(session);
	}
	
}
