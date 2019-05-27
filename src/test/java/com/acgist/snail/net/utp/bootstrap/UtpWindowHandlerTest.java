package com.acgist.snail.net.utp.bootstrap;

import java.nio.ByteBuffer;

import org.junit.Test;

import com.acgist.snail.system.exception.NetException;

public class UtpWindowHandlerTest {

	@Test
	public void test() throws NetException {
		UtpWindowHandler handler = UtpWindowHandler.newInstance(0, (short) 0);
		long begin = System.currentTimeMillis();
		for (int i = 1; i < 100000; i++) {
			ByteBuffer buffer = handler.receive(0, (short) i, ByteBuffer.allocate(10));
			if(buffer == null) {
				System.out.println(i);
			}
		}
		System.out.println(System.currentTimeMillis() - begin);
	}

}
