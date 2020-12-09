package com.acgist.snail.context.recycle.windows;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

public class WindowsRecycleTest extends Performance {

	@Test
	public void test() {
		WindowsRecycle recycle = new WindowsRecycle("E://DD.txt");
		recycle.delete();
	}
	
}
