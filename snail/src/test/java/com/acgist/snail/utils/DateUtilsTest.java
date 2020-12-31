package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.junit.jupiter.api.Test;

public class DateUtilsTest extends Performance {

	@Test
	public void testFormat() {
		this.log(DateUtils.format(1000000));
		this.log(DateUtils.format(100000));
		this.log(DateUtils.format(10000));
		this.log(DateUtils.format(1000));
		this.log(DateUtils.format(10));
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
	public void testDateToCosted() {
		this.costed(100000, () -> DateUtils.dateFormat(new Date()));
		this.costed(100000, () -> DateUtils.localDateTimeFormat(LocalDateTime.now()));
		this.costed(100000, () -> DateUtils.dateFormat(new Date(), "yyyy-MM-dd"));
		this.costed(100000, () -> DateUtils.localDateTimeFormat(LocalDateTime.now(), "yyyy-MM-dd"));
	}
	
}
