package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Date;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.acgist.snail.pojo.entity.TaskEntity;

class BeanUtilsTest extends Performance {

	@Test
	void testNewInstance() {
		final var task = BeanUtils.newInstance(TaskEntity.class);
		assertNotNull(task);
		this.costed(100000, () -> BeanUtils.newInstance(TaskEntity.class));
	}
	
	@Test
	void testToString() {
		final var task = new TaskEntity();
		task.setId("1234");
		task.setCompletedDate(new Date());
		assertEquals(null, BeanUtils.toString(null));
		this.log(BeanUtils.toString(task));
		this.log(BeanUtils.toString(task, "1234"));
	}
	
	@Test
	void testProperty() {
		final var entity = new TaskEntity();
		entity.setId("1234");
		this.log(BeanUtils.properties(TaskEntity.class));
		this.log(BeanUtils.properties(entity, BeanUtils.properties(TaskEntity.class)));
		BeanUtils.properties(entity, Map.of("createDate", new Date(), "name", "acgist"));
		this.log(BeanUtils.properties(entity, BeanUtils.properties(TaskEntity.class)));
		assertEquals("acgist", entity.getName());
		assertEquals("1234", PropertyDescriptor.newInstance(entity).get("id"));
	}
	
}
