package com.acgist.snail.context;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

public class TrackerContextTest extends Performance {

	@Test
	public void testTrackerContext() {
		assertNotNull(TrackerContext.getInstance());
	}
	
}
