package com.acgist.snail.context.recycle.windows;

import org.junit.jupiter.api.Test;

import com.acgist.snail.BaseTest;

public class WindowsRecycleTest extends BaseTest {

	@Test
	public void test() {
		WindowsRecycle recycle = new WindowsRecycle("E://DD.txt");
		recycle.delete();
	}
	
}
