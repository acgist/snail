package com.acgist.snail.net.torrent.tracker.bootstrap.impl;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerClient;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerManager;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.utils.Performance;

public class TrackerClientUdpTest extends Performance {

	@Test
	public void testAnnounce() throws NetException, DownloadException {
		String path = "E:/snail/hyrz.torrent";
//		String path = "E:/snail/12345.torrent";
		TorrentSession session = TorrentManager.getInstance().newTorrentSession(path);
		var list = TrackerManager.getInstance().clients("udp://explodie.org:6969/announce", null);
//		var list = TrackerManager.getInstance().clients("udp://tracker.moeking.me:6969/announce", null);
//		var list = TrackerManager.getInstance().clients("udp://retracker.akado-ural.ru/announce", null);
		TrackerClient client = list.get(0);
		client.announce(1000, session);
//		client.scrape(1000, session);
		this.pause();
	}
	
}
