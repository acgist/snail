package com.acgist.snail.context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.acgist.snail.config.DownloadConfig;
import com.acgist.snail.context.entity.Entity;
import com.acgist.snail.context.entity.TaskEntity;
import com.acgist.snail.gui.recycle.RecycleContext;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.utils.FileUtils;

/**
 * 实体上下文
 * 
 * @author acgist
 */
public final class EntityContext implements IContext {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EntityContext.class);
	
	private static final EntityContext INSTANCE = new EntityContext();
	
	public static final EntityContext getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 实体文件
	 */
	private static final String ENTITY_FILE_PATH = "./config/snail.entities";

	/**
	 * 实体列表
	 */
	private final List<Entity> allEntities;
	
	private EntityContext() {
		this.allEntities = new ArrayList<>();
	}
	
	/**
	 * 保存实体
	 * 
	 * @param entity 实体
	 */
	public void save(Entity entity) {
		EntityException.requireNotNull(entity);
		EntityException.requireNull(entity.getId());
		entity.setId(UUID.randomUUID().toString());
		entity.setCreateDate(new Date());
		entity.setModifyDate(new Date());
		LOGGER.debug("保存实体：{}", entity);
		synchronized (this) {
			this.allEntities.add(entity);
		}
		this.persistent();
	}

	/**
	 * 更新实体
	 * 
	 * @param entity 实体
	 */
	public void update(Entity entity) {
		EntityException.requireNotNull(entity);
		EntityException.requireNotNull(entity.getId());
		entity.setModifyDate(new Date());
		LOGGER.debug("更新实体：{}", entity);
		this.persistent();
	}
	
	/**
	 * 删除实体
	 * 
	 * @param id 实体ID
	 * 
	 * @return 是否删除成功
	 */
	public boolean delete(String id) {
		LOGGER.debug("删除实体：{}", id);
		boolean success = false;
		synchronized (this) {
			success = this.allEntities.removeIf(entity -> entity.getId().equals(id));
		}
		if(success) {
			// 删除成功保存实体
			this.persistent();
		} else {
			LOGGER.warn("删除实体无效：{}", id);
		}
		return success;
	}
	
	/**
	 * @return 所有任务实体列表
	 */
	public List<TaskEntity> allTask() {
		synchronized (this) {
			return this.allEntities.stream()
				.map(v -> {
					if(v instanceof TaskEntity entity) {
						return entity;
					} else {
						return null;
					}
				})
				.filter(Objects::nonNull)
				.toList();
		}
	}
	
	/**
	 * 删除任务
	 * 
	 * @param entity 任务
	 * 
	 * @return 是否删除成功
	 */
	public boolean delete(TaskEntity entity) {
		EntityException.requireNotNull(entity);
		EntityException.requireNotNull(entity.getId());
		LOGGER.debug("删除任务：{}", entity);
		if(DownloadConfig.getDelete()) {
			// 是否删除文件
			final String file = entity.getFile();
			// 回收文件
			if(!RecycleContext.recycle(file)) {
				// 删除文件
				FileUtils.delete(file);
			}
		}
		return this.delete(entity.getId());
	}
	
	/**
	 * 加载实体
	 */
	public void load() {
		final File file = new File(ENTITY_FILE_PATH);
		if(!file.exists()) {
			LOGGER.debug("加载实体文件无效：{}", file);
			return;
		}
		try (
			final ObjectInput input = new ObjectInputStream(new FileInputStream(file));
		) {
			final List<?> list = (List<?>) input.readObject();
			synchronized (this) {
				this.allEntities.clear();
				list.forEach(object -> {
					if(object instanceof Entity entity) {
						this.allEntities.add(entity);
					} else {
						LOGGER.warn("未知实体类型：{}", object);
					}
				});
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug("加载实体数量：{}", this.allEntities.size());
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			LOGGER.error("加载实体异常", e);
		}
	}
	
	/**
	 * 保存实体
	 */
	public void persistent() {
		final List<Entity> list = new ArrayList<>();
		synchronized (this) {
			list.addAll(this.allEntities);
		}
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("保存实体数量：{}", list.size());
		}
		final File file = new File(ENTITY_FILE_PATH);
		FileUtils.buildParentFolder(file);
		try (
			final ObjectOutput output = new ObjectOutputStream(new FileOutputStream(file));
		) {
			output.writeObject(list);
		} catch (IOException e) {
			LOGGER.error("保存实体异常", e);
		}
	}
	
}
