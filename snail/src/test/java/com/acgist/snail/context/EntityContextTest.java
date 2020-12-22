package com.acgist.snail.context;

import org.junit.jupiter.api.Test;

import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.utils.Performance;

public class EntityContextTest extends Performance {
	
	@Test
	public void testLoad() {
		EntityContext.getInstance().allTask().forEach(this::log);
		EntityContext.getInstance().allConfig().forEach(this::log);
	}

	@Test
	public void testPersistent() {
		final TaskEntity taskEntity = new TaskEntity();
		taskEntity.setId("1234");
		EntityContext.getInstance().save(taskEntity);
		EntityContext.getInstance().persistent();
		this.costed(10000, () -> EntityContext.getInstance().persistent());
	}
	
}
