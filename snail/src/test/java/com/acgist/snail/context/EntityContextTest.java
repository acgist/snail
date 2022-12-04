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
import com.acgist.snail.context.entity.ConfigEntity;
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
	void testCosted() {
		this.costed(10000, () -> EntityContext.getInstance().load());
		this.costed(10000, () -> EntityContext.getInstance().persistent());
		assertNotNull(EntityContext.getInstance());
	}

	@Test
	void testAll() {
		EntityContext.getInstance().allTask().forEach(this::log);
		EntityContext.getInstance().allConfig().forEach(this::log);
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
	}
	
	@Test
	@Order(3)
	void testSaveConfig() {
		final var context = EntityContext.getInstance();
		final ConfigEntity entity = new ConfigEntity();
		entity.setName("acgist");
		entity.setValue("测试");
		context.save(entity);
		assertNotNull(entity.getId());
		assertThrows(EntityException.class, () -> context.save(entity));
	}

	@Test
	@Order(4)
	void testUpdateConfig() {
		final var context = EntityContext.getInstance();
		final ConfigEntity entity = new ConfigEntity();
		assertThrows(EntityException.class, () -> context.update(entity));
		entity.setName("acgist");
		entity.setValue("测试");
		final Date modifyDate = new Date(System.currentTimeMillis() - 1000);
		entity.setModifyDate(modifyDate);
		context.save(entity);
		context.update(entity);
		assertNotEquals(modifyDate.getTime(), entity.getModifyDate().getTime());
	}
	
	@Test
	@Order(5)
	void testDeleteConfig() {
		final var context = EntityContext.getInstance();
		final var list = new ArrayList<>(context.allConfig());
		list.forEach(entity -> assertTrue(context.delete(entity)));
	}

	@Test
	@Order(6)
	void testFindConfig() {
		final var context = EntityContext.getInstance();
		final ConfigEntity entity = new ConfigEntity();
		entity.setName("acgist");
		entity.setValue("测试");
		context.save(entity);
		final var config = context.findConfig("acgist");
		assertNotNull(config);
		this.log(config.getName() + "=" + config.getValue());
	}
	
	@Test
	@Order(7)
	void testMergeConfig() {
		final var context = EntityContext.getInstance();
		context.allConfig().forEach(this::log);
		context.mergeConfig("acgist", "1234");
		context.allConfig().forEach(this::log);
		assertEquals("1234", context.findConfig("acgist").getValue());
		context.mergeConfig("acgist", "4321");
		context.allConfig().forEach(this::log);
		assertEquals("4321", context.findConfig("acgist").getValue());
	}
	
}
