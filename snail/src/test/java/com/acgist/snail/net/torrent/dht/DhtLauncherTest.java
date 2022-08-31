package com.acgist.snail.net.torrent.dht;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.PeerConfig.Source;
import com.acgist.snail.context.NodeContext;
import com.acgist.snail.context.PeerContext;
import com.acgist.snail.context.StatisticsContext;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.pojo.InfoHash;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.utils.Performance;

class DhtLauncherTest extends Performance {

	@Test
	void testDhtLauncher() throws DownloadException {
		if(NodeContext.getInstance().nodes().isEmpty()) {
			this.log("没有系统节点");
			return;
		}
		final String infoHashHex = "261adf9754a0eece8e2a228cda4e46102ae86629";
		final DhtLauncher launcher = DhtLauncher.newInstance(TorrentSession.newInstance(InfoHash.newInstance(infoHashHex), null));
		launcher.put("127.0.0.1", 18888);
		PeerContext.getInstance().newPeerSession(infoHashHex, StatisticsContext.getInstance().statistics(), "128.0.0.1", 18888, Source.CONNECT);
		launcher.run();
		this.log(NodeContext.getInstance().nodes().size());
		assertTrue(PeerContext.getInstance().isNotEmpty(infoHashHex));
	}
	
}
