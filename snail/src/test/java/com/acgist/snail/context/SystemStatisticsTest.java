package com.acgist.snail.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.pojo.session.StatisticsSession;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.ThreadUtils;

public class SystemStatisticsTest extends Performance {

	@Test
	public void testSystemStatistics() {
		if(SKIP) {
			this.log("跳过系统统计测试");
			return;
		}
		assertNotNull(SystemStatistics.getInstance());
		assertNotNull(SystemStatistics.getInstance().statistics());
		assertEquals(0, SystemStatistics.getInstance().uploadSpeed());
		assertEquals(0, SystemStatistics.getInstance().downloadSpeed());
		final StatisticsSession session = new StatisticsSession(false, SystemStatistics.getInstance().statistics());
		session.uploadLimit(1024);
		session.downloadLimit(1024);
		ThreadUtils.sleep(4000); // 统计等待
		assertNotEquals(0, session.uploadSpeed());
		assertNotEquals(0, session.downloadSpeed());
		assertNotEquals(0, SystemStatistics.getInstance().uploadSpeed());
		assertNotEquals(0, SystemStatistics.getInstance().downloadSpeed());
	}
	
}
