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
	
	@Test
	public void testEnumProperty() {
		PropertyDescriptor descriptor = new PropertyDescriptor("status", TaskEntity.class);
		if(descriptor.getPropertyType().isEnum()) {
			final var enums = descriptor.getPropertyType().getEnumConstants();
			for (Object object : enums) {
				if(object.toString().equals("PAUSE")) {
					this.log(object);
					this.log(object.getClass());
				}
			}
//			this.log(Enum.valueOf(((Class<Enum>) descriptor.getPropertyType()), "PAUSE"));
		}
	}
	
}
