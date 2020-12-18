package com.acgist.snail.context.initializer.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.protocol.ProtocolManager;
import com.acgist.snail.utils.Performance;

public class ProtocolInitializerTest extends Performance {

	@Test
	public void testProtocolInitializer() throws DownloadException {
		ProtocolInitializer.newInstance().sync();
		assertTrue(ProtocolManager.getInstance().available());
	}
	
	@Test
	public void testCosted() {
		final long costed = this.costed(100000, () -> ProtocolInitializer.newInstance().sync());
		assertTrue(costed < 30000);
	}
	
}
