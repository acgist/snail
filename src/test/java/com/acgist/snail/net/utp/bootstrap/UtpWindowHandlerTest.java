package com.acgist.snail.net.utp.bootstrap;

import java.nio.ByteBuffer;

import org.junit.Test;

import com.acgist.snail.system.exception.NetException;

public class UtpWindowHandlerTest {

	@Test
	public void test() throws NetException {
		UtpWindowHandler handler = new UtpWindowHandler((short) 0);
		long begin = System.currentTimeMillis();
		for (int i = 1; i < 100000; i++) {
			ByteBuffer buffer = handler.put(0, (short) i, ByteBuffer.allocate(10));
			if(buffer == null) {
				System.out.println(i);
			}
		}
		System.out.println(System.currentTimeMillis() - begin);
	}

}
