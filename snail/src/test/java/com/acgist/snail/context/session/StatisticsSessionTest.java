package com.acgist.snail.context.session;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

class StatisticsSessionTest extends Performance {

	@Test
	void testStatisticsSession() {
		final StatisticsSession parent = new StatisticsSession();
		final StatisticsSession session = new StatisticsSession(true, parent);
		session.upload(1024);
		assertEquals(1024, parent.getUploadSize());
		assertEquals(1024, session.getUploadSize());
		session.download(1024);
		assertEquals(1024, parent.getDownloadSize());
		assertEquals(1024, session.getDownloadSize());
	}
	
}
