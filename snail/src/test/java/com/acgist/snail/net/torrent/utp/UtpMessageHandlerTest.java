package com.acgist.snail.net.torrent.utp;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.torrent.TorrentServer;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.Performance;

public class UtpMessageHandlerTest extends Performance {

	@Test
	public void test() {
		var socketAddress = NetUtils.buildSocketAddress("45.14.148.240", 50007);
		var handler = new UtpMessageHandler((short) 1000, socketAddress);
		handler.handle(TorrentServer.getInstance().channel(), socketAddress);
		var connect = handler.connect();
		this.log("连接：{}", connect);
//		handler.resetAndClose();
		this.pause();
	}
	
}
