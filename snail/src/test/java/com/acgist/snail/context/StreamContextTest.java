package com.acgist.snail.context;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.ThreadUtils;

public class StreamContextTest extends Performance {

	@Test
	public void testNewRemove() {
		final StreamContext context = StreamContext.getInstance();
		final InputStream input = new ByteArrayInputStream(new byte[1024]);
		final var session = context.newStreamSession(input);
		assertTrue(session.remove());
	}
	
	@Test
	public void testCheckLive() {
		if(SKIP) {
			this.log("跳过CheckLive检测");
			return;
		}
		final StreamContext context = StreamContext.getInstance();
		final InputStream input = new ByteArrayInputStream(new byte[1024]);
		final var session = context.newStreamSession(input);
		assertTrue(session.checkLive());
		ThreadUtils.sleep(40 * 1000);
		assertFalse(session.checkLive());
	}
	
}
