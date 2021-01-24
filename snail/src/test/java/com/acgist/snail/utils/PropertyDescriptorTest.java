package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
		final PropertyDescriptor descriptor = PropertyDescriptor.newInstance("id", task.getClass());
		assertEquals(id, descriptor.get(task));
	}
	
	@Test
	public void testSetter() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		final String id = "1234";
		final TaskEntity task = new TaskEntity();
		assertNull(task.getId());
		PropertyDescriptor.newInstance("id", task.getClass()).set(task, id);
		assertEquals(id, task.getId());
	}
	
	@Test
	public void testEnumProperty() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		boolean find = false;
		final TaskEntity task = new TaskEntity();
		task.setStatus(Status.AWAIT);
		final PropertyDescriptor descriptor = PropertyDescriptor.newInstance("status", TaskEntity.class);
		if(descriptor.getPropertyType().isEnum()) {
			final var enums = descriptor.getPropertyType().getEnumConstants();
			for (Object object : enums) {
				if(object == descriptor.get(task)) {
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
		this.costed(100000, () -> {
			try {
				PropertyDescriptor.newInstance("id", task.getClass()).get(task);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				this.log(e);
			}
		});
		this.costed(100000, () -> {
			try {
				PropertyDescriptor.newInstance("id", task.getClass()).set(task, "1234");
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				this.log(e);
			}
		});
	}
	
}
