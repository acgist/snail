package com.acgist.main;

import org.junit.Test;

import com.acgist.snail.net.tracker.TrackerClientManager;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.torrent.TorrentCoder;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.ThreadUtils;

public class TrackerClientTest {

	@Test
	public void test() throws NetException, DownloadException {
		String path = "e:/tmp/11e38a5270e15c60534ca48977b7d77a3c4f6340.torrent";
		TorrentCoder decoder = TorrentCoder.newInstance(path);
		TorrentSession session = decoder.torrentSession();
		System.out.println(session.infoHash().hashHex());
		var client = TrackerClientManager.getInstance().tracker("udp://exodus.desync.com:6969/announce");
		client.announce(session);
		ThreadUtils.sleep(1000000);
	}
	
}
