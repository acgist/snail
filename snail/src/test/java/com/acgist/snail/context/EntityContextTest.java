package com.acgist.snail.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Date;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.EntityException;
import com.acgist.snail.logger.LoggerConfig;
import com.acgist.snail.pojo.ITaskSession.FileType;
import com.acgist.snail.pojo.entity.ConfigEntity;
import com.acgist.snail.pojo.entity.TaskEntity;
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
		EntityContext.getInstance().save(entity);
		assertNotNull(entity.getId());
		assertThrows(EntityException.class, () -> EntityContext.getInstance().save(entity));
	}

	@Test
	@Order(1)
	void testUpdateTask() {
		final TaskEntity entity = new TaskEntity();
		assertThrows(EntityException.class, () -> EntityContext.getInstance().update(entity));
		entity.setName("测试");
		entity.setType(Type.HTTP);
		entity.setFileType(FileType.VIDEO);
		final Date modifyDate = new Date(System.currentTimeMillis() - 1000);
		entity.setModifyDate(modifyDate);
		EntityContext.getInstance().save(entity);
		EntityContext.getInstance().update(entity);
		assertNotEquals(modifyDate.getTime(), entity.getModifyDate().getTime());
	}
	
	@Test
	@Order(2)
	void testDeleteTask() {
		final var list = new ArrayList<>(EntityContext.getInstance().allTask());
		list.forEach(entity -> assertTrue(EntityContext.getInstance().delete(entity)));
	}
	
	@Test
	@Order(3)
	void testSaveConfig() {
		final ConfigEntity entity = new ConfigEntity();
		entity.setName("acgist");
		entity.setValue("测试");
		EntityContext.getInstance().save(entity);
		assertNotNull(entity.getId());
		assertThrows(EntityException.class, () -> EntityContext.getInstance().save(entity));
	}

	@Test
	@Order(4)
	void testUpdateConfig() {
		final ConfigEntity entity = new ConfigEntity();
		assertThrows(EntityException.class, () -> EntityContext.getInstance().update(entity));
		entity.setName("acgist");
		entity.setValue("测试");
		final Date modifyDate = new Date(System.currentTimeMillis() - 1000);
		entity.setModifyDate(modifyDate);
		EntityContext.getInstance().save(entity);
		EntityContext.getInstance().update(entity);
		assertNotEquals(modifyDate.getTime(), entity.getModifyDate().getTime());
	}
	
	@Test
	@Order(5)
	void testDeleteConfig() {
		final var list = new ArrayList<>(EntityContext.getInstance().allConfig());
		list.forEach(entity -> assertTrue(EntityContext.getInstance().delete(entity)));
	}

	@Test
	@Order(6)
	void testFindConfigByName() {
		final ConfigEntity entity = new ConfigEntity();
		entity.setName("acgist");
		entity.setValue("测试");
		EntityContext.getInstance().save(entity);
		final var config = EntityContext.getInstance().findConfigByName("acgist");
		assertNotNull(config);
		this.log(config.getName() + "=" + config.getValue());
	}
	
	@Test
	@Order(7)
	void testDeleteConfigByName() {
		final ConfigEntity entity = new ConfigEntity();
		entity.setName("acgist");
		entity.setValue("测试");
		EntityContext.getInstance().save(entity);
		assertNotNull(EntityContext.getInstance().findConfigByName("acgist"));
		while(EntityContext.getInstance().findConfigByName("acgist") != null) {
			EntityContext.getInstance().deleteConfigByName("acgist");
		}
		assertNull(EntityContext.getInstance().findConfigByName("acgist"));
	}
	
	@Test
	@Order(8)
	void testMergeConfig() {
		EntityContext.getInstance().allConfig().forEach(this::log);
		EntityContext.getInstance().mergeConfig("acgist", "1234");
		EntityContext.getInstance().allConfig().forEach(this::log);
		assertEquals("1234", EntityContext.getInstance().findConfigByName("acgist").getValue());
		EntityContext.getInstance().mergeConfig("acgist", "4321");
		EntityContext.getInstance().allConfig().forEach(this::log);
		assertEquals("4321", EntityContext.getInstance().findConfigByName("acgist").getValue());
	}
	
}
