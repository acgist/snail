package com.acgist.snail.context.recycle;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

public class RecycleManagerTest extends Performance {

	@Test
	public void testRecycleManager() {
		assertNotNull(RecycleManager.newInstance("E://snail/tmp.txt"));
	}
	
	@Test
	public void testRecycle() throws IOException {
		final String path = "E://snail/" + System.currentTimeMillis() + ".txt";
		final File file = new File(path);
		file.createNewFile();
		assertTrue(file.exists());
		RecycleManager.recycle(path);
		assertFalse(file.exists());
	}
	
}
