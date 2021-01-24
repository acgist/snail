package com.acgist.snail.utils;

import org.junit.jupiter.api.Test;

import com.acgist.snail.pojo.entity.TaskEntity;

public class BeanUtilsTest extends Performance {

	@Test
	public void testRead() {
		TaskEntity entity = new TaskEntity();
		entity.setId("1234");
		this.log(BeanUtils.propertyValue(entity, "id"));
	}
	
}
