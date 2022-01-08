package com.acgist.snail.logger;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.utils.Performance;

class LoggerTest extends Performance {

	@Test
	void testCosted() {
		// TODO：想想办法搞到400毫秒
		final long costed = this.costed(100000, () -> this.log("----{}----", System.currentTimeMillis()));
		assertTrue(costed < 3000);
	}
	
	@Test
	void testLevel() {
		String arga = null;
		String argb = "";
		final TaskEntity taskEntity = new TaskEntity();
		LOGGER.debug("debug：{}-{}-{}-{}", arga, argb, taskEntity);
		LOGGER.info("info：{}-{}-{}-{}", arga, argb, taskEntity);
		LOGGER.warn("warn：{}-{}-{}-{}", arga, argb, taskEntity);
		LOGGER.error("error：{}-{}-{}-{}", arga, argb, taskEntity);
		try {
			throw new NetException("错误测试");
		} catch (Exception e) {
			LOGGER.debug("debug", e);
			LOGGER.info("info", e);
			LOGGER.warn("warn", e);
			LOGGER.error("error", e);
		}
		assertNotNull(LOGGER);
	}
	
}
