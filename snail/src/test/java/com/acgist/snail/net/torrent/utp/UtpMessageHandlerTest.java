package com.acgist.snail.net.torrent.utp;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.torrent.TorrentServer;
import com.acgist.snail.net.torrent.peer.PeerSubMessageHandler;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.ThreadUtils;

class UtpMessageHandlerTest extends Performance {

	@Test
	void testServer() {
		assertDoesNotThrow(() -> {
			TorrentServer.getInstance();
			ThreadUtils.sleep(100000);
		});
	}
	
	@Test
	void testConnect() {
		final var socketAddress = NetUtils.buildSocketAddress("127.0.0.1", 18888);
		final var handler = new UtpMessageHandler(PeerSubMessageHandler.newInstance(), socketAddress);
		handler.handle(TorrentServer.getInstance().channel());
		var connect = handler.connect();
		this.log("连接：{}", connect);
		assertTrue(connect);
		connect = handler.connect();
		this.log("连接：{}", connect);
		assertTrue(connect);
	}
	
}
