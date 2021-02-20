package com.acgist.snail.context;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

class TrackerContextTest extends Performance {

	@Test
	void testTrackerContext() {
		assertNotNull(TrackerContext.getInstance());
	}
	
	@Test
	void testError() {
		assertNotNull(TrackerContext.getInstance().sessions("acgist://www.acgist.com"));
	}
	
}
