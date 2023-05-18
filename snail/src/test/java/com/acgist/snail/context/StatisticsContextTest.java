package com.acgist.snail.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.session.StatisticsSession;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.ThreadUtils;

class StatisticsContextTest extends Performance {

	@Test
	void testStatisticsContext() {
		assertNotNull(StatisticsContext.getInstance());
		assertNotNull(StatisticsContext.getInstance().getStatistics());
		assertEquals(0, StatisticsContext.getInstance().getUploadSpeed());
		assertEquals(0, StatisticsContext.getInstance().getDownloadSpeed());
		final StatisticsSession session = new StatisticsSession(false, StatisticsContext.getInstance().getStatistics());
		session.uploadLimit(1024);
		session.downloadLimit(1024);
		ThreadUtils.sleep(SystemConfig.REFRESH_INTERVAL_MILLIS); // 统计等待
		assertNotEquals(0, session.getUploadSpeed());
		assertNotEquals(0, session.getDownloadSpeed());
		assertNotEquals(0, StatisticsContext.getInstance().getUploadSpeed());
		assertNotEquals(0, StatisticsContext.getInstance().getDownloadSpeed());
	}
	
}
