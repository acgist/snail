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
		assertTrue(context.removeStreamSession(session));
	}
	
	@Test
	public void testCheckLive() {
		final StreamContext context = StreamContext.getInstance();
		final InputStream input = new ByteArrayInputStream(new byte[1024]);
		final var session = context.newStreamSession(input);
		assertTrue(session.checkLive());
		this.costed(1, () -> {
			ThreadUtils.sleep(40 * 1000);
			this.log(session.checkLive());
			assertFalse(session.checkLive());
		});
	}
	
}
