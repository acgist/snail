package com.acgist.snail.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.pojo.session.StatisticsSession;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.ThreadUtils;

public class StatisticsContextTest extends Performance {

	@Test
	public void testStatisticsContext() {
		if(SKIP_COSTED) {
			this.log("跳过testStatisticsContext测试");
			return;
		}
		assertNotNull(StatisticsContext.getInstance());
		assertNotNull(StatisticsContext.getInstance().statistics());
		assertEquals(0, StatisticsContext.getInstance().uploadSpeed());
		assertEquals(0, StatisticsContext.getInstance().downloadSpeed());
		final StatisticsSession session = new StatisticsSession(false, StatisticsContext.getInstance().statistics());
		session.uploadLimit(1024);
		session.downloadLimit(1024);
		ThreadUtils.sleep(4000); // 统计等待
		assertNotEquals(0, session.uploadSpeed());
		assertNotEquals(0, session.downloadSpeed());
		assertNotEquals(0, StatisticsContext.getInstance().uploadSpeed());
		assertNotEquals(0, StatisticsContext.getInstance().downloadSpeed());
	}
	
}
