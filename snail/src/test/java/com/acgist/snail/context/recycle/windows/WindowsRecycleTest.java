package com.acgist.snail.context.recycle.windows;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

public class WindowsRecycleTest extends Performance {

	@Test
	public void testDelete() throws IOException {
		final String path = "E://snail/" + System.currentTimeMillis() + ".txt";
		final File file = new File(path);
		file.createNewFile();
		assertTrue(file.exists());
		new WindowsRecycle(path).delete();
		assertFalse(file.exists());
	}
	
}
