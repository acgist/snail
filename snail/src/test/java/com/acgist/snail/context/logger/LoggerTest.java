package com.acgist.snail.context.logger;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.utils.Performance;

public class LoggerTest extends Performance {

	@Test
	public void testCost() {
		this.cost(100000, 10, () -> {
			this.log("----" + System.currentTimeMillis());
//			ThreadUtils.sleep(10);
		});
	}

	@Test
	public void testLevel() {
		String arga = null;
		String argb = "";
		TaskEntity taskEntity = new TaskEntity();
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
	}
	
}
