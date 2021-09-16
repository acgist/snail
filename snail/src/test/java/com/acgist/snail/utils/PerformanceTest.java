package com.acgist.snail.utils;

import org.junit.jupiter.api.Test;

class PerformanceTest extends Performance {

	@Test
	void testLog() {
		this.log(null);
		this.log("1");
		this.log(null, "1");
		this.log(null, null, null);
		this.log(null, new Object[] { null });
		this.log(null, new Object[] { null, null });
		this.log(null, new Object[] {});
		this.log(null, "1", "2", 3);
	}
	
}
