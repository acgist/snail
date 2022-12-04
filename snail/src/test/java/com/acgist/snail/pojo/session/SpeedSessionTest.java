package com.acgist.snail.pojo.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.session.SpeedSession;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.ThreadUtils;

class SpeedSessionTest extends Performance {

	@Test
	void testSpeedSession() {
		final SpeedSession session = new SpeedSession();
		session.buffer(1024);
		session.buffer(1024);
		ThreadUtils.sleep(4000);
		session.buffer(1024);
		session.buffer(1024);
		this.log(session.speed());
		assertTrue(session.speed() <= 1024);
		session.reset();
		assertTrue(session.speed() <= 1024);
		ThreadUtils.sleep(4000);
		assertEquals(0, session.speed());
	}
	
}
