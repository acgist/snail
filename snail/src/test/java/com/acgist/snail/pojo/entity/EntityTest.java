package com.acgist.snail.pojo.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.entity.TaskEntity;
import com.acgist.snail.utils.Performance;

class EntityTest extends Performance {

	@Test
	void testEntity() {
		final var task = new TaskEntity();
		task.setId("1234");
		final var eq = new TaskEntity();
		eq.setId("1234");
		final var df = new TaskEntity();
		df.setId("4321");
		final Object ob = task;
		assertEquals(task, eq);
		assertEquals(task, ob);
		assertNotEquals(task, df);
	}
	
}
