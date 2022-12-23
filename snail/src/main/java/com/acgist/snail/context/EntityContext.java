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
	 * 实体文件：{@value}
	 */
	private static final String ENTITY_FILE_PATH = "./config/snail.entities";

	/**
	 * 任务列表
	 */
	private final List<TaskEntity> taskEntities;
	
	private EntityContext() {
		this.taskEntities = new ArrayList<>();
	}
	
	/**
	 * @return 所有任务列表
	 */
	public List<TaskEntity> allTask() {
		return new ArrayList<>(this.taskEntities);
	}
	
	/**
	 * 设置保存实体信息
	 * 
	 * @param entity 实体
	 */
	private void preSave(Entity entity) {
		EntityException.requireNotNull(entity);
		EntityException.requireNull(entity.getId());
		entity.setId(UUID.randomUUID().toString());
		entity.setCreateDate(new Date());
		entity.setModifyDate(new Date());
		LOGGER.debug("保存实体：{}", entity);
	}
	
	/**
	 * 设置更新实体信息
	 * 
	 * @param entity 实体
	 */
	private void preUpdate(Entity entity) {
		EntityException.requireNotNull(entity);
		EntityException.requireNotNull(entity.getId());
		entity.setModifyDate(new Date());
		LOGGER.debug("更新实体：{}", entity);
	}
	
	/**
	 * 删除实体
	 * 
	 * @param id 实体ID
	 * 
	 * @return 是否删除成功
	 */
	private boolean delete(String id) {
		LOGGER.debug("删除实体：{}", id);
		boolean success = false;
		synchronized (this) {
			success = this.taskEntities.removeIf(entity -> entity.getId().equals(id));
		}
		if(success) {
			// 删除成功保存实体
			this.persistent();
		}
		return success;
	}
	
	/**
	 * 保存任务
	 * 
	 * @param entity 任务
	 */
	public void save(TaskEntity entity) {
		this.preSave(entity);
		synchronized (this) {
			this.taskEntities.add(entity);
		}
		this.persistent();
	}

	/**
	 * 更新任务
	 * 
	 * @param entity 任务
	 */
	public void update(TaskEntity entity) {
		this.preUpdate(entity);
		this.persistent();
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
		LOGGER.debug("删除任务：{}", entity);
		if(DownloadConfig.getDelete()) {
			final var file = entity.getFile();
			if(RecycleContext.recycle(file)) {
				// 回收文件
				LOGGER.debug("删除任务回收文件成功：{}", file);
			} else {
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
		try (final ObjectInput input = new ObjectInputStream(new FileInputStream(file))) {
			final List<?> list = (List<?>) input.readObject();
			synchronized (this) {
				this.taskEntities.clear();
				list.forEach(object -> {
					if(object instanceof TaskEntity entity) {
						this.taskEntities.add(entity);
					} else {
						LOGGER.warn("未知实体类型：{}", object);
					}
				});
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug("加载任务实体数量：{}", this.taskEntities.size());
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
			list.addAll(this.taskEntities);
		}
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("保存实体：{}", list.size());
		}
		final File file = new File(ENTITY_FILE_PATH);
		FileUtils.buildParentFolder(file);
		try (final ObjectOutput output = new ObjectOutputStream(new FileOutputStream(file))) {
			output.writeObject(list);
		} catch (IOException e) {
			LOGGER.error("保存实体异常", e);
		}
	}
	
}
