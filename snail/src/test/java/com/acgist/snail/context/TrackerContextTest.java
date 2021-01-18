package com.acgist.snail.context;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

public class TrackerContextTest extends Performance {

	@Test
	public void testTrackerContext() {
		TrackerContext.getInstance();
	}
	
}
