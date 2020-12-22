package com.acgist.snail.utils;

import java.time.LocalDateTime;
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
	public void testDateToCosted() {
		this.costed(100000, () -> DateUtils.dateFormat(new Date()));
		this.costed(100000, () -> DateUtils.localDateTimeFormat(LocalDateTime.now()));
		this.costed(100000, () -> DateUtils.dateFormat(new Date(), "yyyy-MM-dd"));
		this.costed(100000, () -> DateUtils.localDateTimeFormat(LocalDateTime.now(), "yyyy-MM-dd"));
	}
	
}
