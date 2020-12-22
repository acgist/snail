package com.acgist.snail.logger;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.utils.Performance;

public class LoggerTest extends Performance {

	@Test
	public void testCosted() {
		this.costed(100000, 10, () -> this.log("----" + System.currentTimeMillis()));
	}
	
	@Test
	public void testDateFormatCosted() {
		final String pattern = "yyyy-MM-dd HH:mm:ss";
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		final long oldCosted = this.costed(100000, () -> simpleDateFormat.format(new Date()));
		final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
		final long newCosted = this.costed(100000, () -> dateTimeFormatter.format(LocalDateTime.now()));
		assertTrue(oldCosted > newCosted);
		this.costed(100000, () -> dateTimeFormatter.format(LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault())));
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
