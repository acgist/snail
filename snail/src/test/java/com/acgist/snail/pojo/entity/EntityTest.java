package com.acgist.snail.pojo.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.entity.ConfigEntity;
import com.acgist.snail.context.entity.TaskEntity;
import com.acgist.snail.utils.Performance;

class EntityTest extends Performance {

	@Test
	void testEntity() {
		final var task = new TaskEntity();
		task.setId("1234");
		final var config = new ConfigEntity();
		config.setId("1234");
		final var verify = new ConfigEntity();
		verify.setId("1234");
		final Object taskVerify = task;
		assertEquals(verify, config);
		assertNotEquals(verify, taskVerify);
	}
	
}
