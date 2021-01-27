package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;

import com.acgist.snail.pojo.ITaskSessionStatus.Status;
import com.acgist.snail.pojo.entity.TaskEntity;

public class PropertyDescriptorTest extends Performance {

	@Test
	public void testGetter() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		final String id = "1234";
		final TaskEntity task = new TaskEntity();
		task.setId(id);
		assertEquals(id, PropertyDescriptor.newInstance(task).get("id"));
	}
	
	@Test
	public void testSetter() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		final String id = "1234";
		final TaskEntity task = new TaskEntity();
		assertNull(task.getId());
		PropertyDescriptor.newInstance(task).set("id", id);
		assertEquals(id, task.getId());
	}
	
	@Test
	public void testEnumProperty() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		boolean find = false;
		final TaskEntity task = new TaskEntity();
		task.setStatus(Status.AWAIT);
		final PropertyDescriptor descriptor = PropertyDescriptor.newInstance(task);
		if(descriptor.getPropertyType("status").isEnum()) {
			final var enums = descriptor.getPropertyType("status").getEnumConstants();
			for (Object object : enums) {
				if(object == descriptor.get("status")) {
					find = true;
					break;
				}
			}
		}
		assertTrue(find);
	}

	@Test
	public void testCosted() {
		final TaskEntity task = new TaskEntity();
		final var descriptor = PropertyDescriptor.newInstance(task);
		this.costed(100000, () -> descriptor.get("id"));
		this.costed(100000, () -> descriptor.set("id", "1234"));
		assertNotNull(task);
	}
	
}
