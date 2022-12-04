package com.acgist.snail.pojo.session;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.session.StatisticsSession;
import com.acgist.snail.utils.Performance;

class StatisticsSessionTest extends Performance {

	@Test
	void testStatisticsSession() {
		final StatisticsSession parent = new StatisticsSession();
		final StatisticsSession session = new StatisticsSession(true, parent);
		session.upload(1024);
		assertEquals(1024, parent.uploadSize());
		assertEquals(1024, session.uploadSize());
		session.download(1024);
		assertEquals(1024, parent.downloadSize());
		assertEquals(1024, session.downloadSize());
	}
	
}
