package com.acgist.snail.net.torrent.tracker.bootstrap;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.pojo.session.TrackerSession;
import com.acgist.snail.utils.Performance;

public class UdpTrackerSessionTest extends Performance {

	@Test
	public void testAnnounce() throws NetException, DownloadException {
//		final String path = "E:/snail/hyrz.torrent";
		final String path = "E:/snail/12345.torrent";
		final String announceUrl = "udp://explodie.org:6969/announce";
//		final String announceUrl = "udp://tracker.moeking.me:6969/announce";
//		final String announceUrl = "udp://retracker.akado-ural.ru/announce";
		final TorrentSession torrentSession = TorrentManager.getInstance().newTorrentSession(path);
		final var list = TrackerManager.getInstance().sessions("udp://explodie.org:6969/announce");
		final TrackerSession session = list.stream()
			.filter(value -> value.equalsAnnounceUrl(announceUrl))
			.findFirst()
			.get();
		session.announce(1000, torrentSession);
//		session.scrape(1000, torrentSession);
		this.pause();
	}
	
}
