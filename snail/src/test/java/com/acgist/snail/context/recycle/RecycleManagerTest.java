package com.acgist.snail.context.recycle;

import org.junit.jupiter.api.Test;

import com.acgist.snail.BaseTest;

public class RecycleManagerTest extends BaseTest {

	@Test
	public void testDelete() {
		RecycleManager.newInstance("E:/DD").delete();
//		RecycleManager.newInstance("E:/DD.txt").delete();
//		RecycleManager.newInstance("E:/测试/DD.txt").delete();
	}
	
}
