package com.acgist.snail.utils;

import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;

import com.acgist.snail.pojo.entity.TaskEntity;

public class PropertyDescriptorTest extends Performance {

	@Test
	public void testRead() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		TaskEntity task = new TaskEntity();
		task.setId("1234");
		PropertyDescriptor descriptor = new PropertyDescriptor("id", task.getClass());
		this.log(descriptor.getReadMethod().invoke(task));
	}
	
	@Test
	public void testWrite() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		TaskEntity task = new TaskEntity();
		PropertyDescriptor descriptor = new PropertyDescriptor("id", task.getClass());
		this.log(task.getId());
		descriptor.getWriteMethod().invoke(task, "1234");
		this.log(task.getId());
	}
	
}
