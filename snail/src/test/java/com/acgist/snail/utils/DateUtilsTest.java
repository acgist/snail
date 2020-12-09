package com.acgist.snail.utils;

import org.junit.jupiter.api.Test;

public class DateUtilsTest extends Performance {

	@Test
	public void format() {
		this.log(DateUtils.format(1000000));
		this.log(DateUtils.format(100000));
		this.log(DateUtils.format(10000));
		this.log(DateUtils.format(1000));
		this.log(DateUtils.format(10));
	}
	
}
