package com.acgist.snail.context.initializer.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.torrent.tracker.TrackerManager;
import com.acgist.snail.utils.Performance;

public class TrackerInitializerTest extends Performance {

	@Test
	public void testTrackerInitializer() {
		TrackerInitializer.newInstance().sync();
		assertTrue(TrackerManager.getInstance().sessions().size() > 0);
	}
	
	@Test
	public void testCosted() {
		final long costed = this.costed(100000, () -> TrackerInitializer.newInstance().sync());
		assertTrue(costed < 30000);
	}
	
}
