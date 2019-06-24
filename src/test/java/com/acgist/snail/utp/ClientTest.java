package com.acgist.snail.utp;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Test;

import com.acgist.snail.net.bt.torrent.TorrentServer;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.ThreadUtils;

public class ClientTest {

	@Test
	public void test() throws IOException {
		TorrentServer.getInstance().channel().send(
			ByteBuffer.wrap("1234".getBytes()),
			NetUtils.buildSocketAddress("211.149.212.14", 8888));
		ThreadUtils.sleep(Long.MAX_VALUE);
	}
	
}
