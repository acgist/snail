package com.acgist.snail.context.recycle;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

public class RecycleManagerTest extends Performance {

	@Test
	public void testDelete() {
		RecycleManager.newInstance("E:/DD").delete();
//		RecycleManager.newInstance("E:/DD.txt").delete();
//		RecycleManager.newInstance("E:/测试/DD.txt").delete();
	}
	
}
