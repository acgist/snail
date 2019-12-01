package com.acgist.snail.utp;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Test;

import com.acgist.snail.BaseTest;
import com.acgist.snail.net.torrent.utp.bootstrap.UtpWindow;
import com.acgist.snail.net.torrent.utp.bootstrap.UtpWindowData;

public class UtpWindowHandlerTest extends BaseTest {

	@Test
	public void cos() throws IOException {
		UtpWindow handler = UtpWindow.newInstance();
		long begin = System.currentTimeMillis();
		for (int i = 1; i < 100000; i++) {
			UtpWindowData windowData = handler.receive(0, (short) i, ByteBuffer.allocate(10));
			if(windowData == null) {
				this.log(i);
			}
		}
		this.log(System.currentTimeMillis() - begin);
	}

}
