package com.acgist.snail.net.torrent.tracker;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

public class TrackerManagerTest extends Performance {

	@Test
	public void testTrackerManager() {
		TrackerManager.getInstance();
	}
	
}
