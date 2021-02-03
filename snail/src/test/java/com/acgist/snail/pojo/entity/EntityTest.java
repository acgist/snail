package com.acgist.snail.pojo.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

public class EntityTest extends Performance {

	@Test
	public void testEntity() {
		final var task = new TaskEntity();
		task.setId("1234");
		final var config = new ConfigEntity();
		config.setId("1234");
		final var verify = new ConfigEntity();
		verify.setId("1234");
		final Object taskVerify = task;
		assertFalse(verify.equals(taskVerify));
		assertTrue(verify.equals(config));
	}
	
}
