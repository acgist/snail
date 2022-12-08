package com.acgist.snail.net.torrent.utp;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.UtpConfig;
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
	
	@Test
	void testSelect() {
		final UtpConfig.Type[] types = UtpConfig.Type.values();
		for (UtpConfig.Type type : types) {
			this.log("{}-{}-{}-{}", type, type.type(), type.typeVersion(), Integer.toHexString(type.typeVersion()));
			assertEquals(UtpConfig.Type.of(type.typeVersion()), type);
		}
		this.log("{}-{}-{}", 'd', (int) 'd', Integer.toHexString('d'));
		this.log("{}-{}-{}", 'f', (int) 'f', Integer.toHexString('f'));
		this.log("{}-{}-{}", 'q', (int) 'q', Integer.toHexString('q'));
		assertEquals(0x00, UtpConfig.Type.DATA.type());
		assertEquals(0x01, UtpConfig.Type.DATA.typeVersion());
		assertEquals(0x04, UtpConfig.Type.SYN.type());
		assertEquals(0x41, UtpConfig.Type.SYN.typeVersion());
	}
	
	@Test
	void testCosted() {
		final byte value = 'f';
		this.costed(10000000, () -> {
			boolean a = value == 'd';
			boolean b = value == 'q';
			if(a && b) {
			}
		});
	}
	
}
