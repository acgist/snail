package com.acgist.snail.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Date;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import com.acgist.snail.context.ITaskSession.FileType;
import com.acgist.snail.context.entity.TaskEntity;
import com.acgist.snail.logger.LoggerConfig;
import com.acgist.snail.protocol.Protocol.Type;
import com.acgist.snail.utils.Performance;

class EntityContextTest extends Performance {
	
	@BeforeAll
	static final void load() {
		LoggerConfig.off();
		EntityContext.getInstance().load();
	}
	
	@Test
	@Order(3)
	void testCosted() {
		this.costed(10000, () -> EntityContext.getInstance().load());
		this.costed(10000, () -> EntityContext.getInstance().persistent());
		assertNotNull(EntityContext.getInstance());
	}

	@Test
	@Order(4)
	void testAll() {
		EntityContext.getInstance().allTask().forEach(this::log);
		assertNotNull(EntityContext.getInstance());
	}
	
	@Test
	@Order(0)
	void testSaveTask() {
		final TaskEntity entity = new TaskEntity();
		entity.setName("测试");
		entity.setType(Type.HTTP);
		entity.setFileType(FileType.VIDEO);
		final var context = EntityContext.getInstance();
		context.save(entity);
		assertNotNull(entity.getId());
		assertThrows(EntityException.class, () -> context.save(entity));
	}

	@Test
	@Order(1)
	void testUpdateTask() {
		final TaskEntity entity = new TaskEntity();
		final var context = EntityContext.getInstance();
		assertThrows(EntityException.class, () -> context.update(entity));
		entity.setName("测试");
		entity.setType(Type.HTTP);
		entity.setFileType(FileType.VIDEO);
		final Date modifyDate = new Date(System.currentTimeMillis() - 1000);
		entity.setModifyDate(modifyDate);
		context.save(entity);
		context.update(entity);
		assertNotEquals(modifyDate.getTime(), entity.getModifyDate().getTime());
	}
	
	@Test
	@Order(2)
	void testDeleteTask() {
		final var context = EntityContext.getInstance();
		final var list = new ArrayList<>(context.allTask());
		list.forEach(entity -> assertTrue(context.delete(entity)));
		assertTrue(!list.isEmpty());
		assertEquals(0, context.allTask().size());
	}
	
}
