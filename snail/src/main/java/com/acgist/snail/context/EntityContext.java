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

import com.acgist.snail.IContext;
import com.acgist.snail.config.DownloadConfig;
import com.acgist.snail.context.exception.EntityException;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.pojo.entity.ConfigEntity;
import com.acgist.snail.pojo.entity.Entity;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.utils.FileUtils;

/**
 * <p>实体上下文</p>
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
	 * <p>实体文件：{@value}</p>
	 */
	private static final String ENTITY_FILE_PATH = "./config/snail.entities";

	/**
	 * <p>任务列表</p>
	 */
	private final List<TaskEntity> taskEntities;
	/**
	 * <p>配置列表</p>
	 */
	private final List<ConfigEntity> configEntities;
	
	private EntityContext() {
		this.taskEntities = new ArrayList<>();
		this.configEntities = new ArrayList<>();
	}
	
	/**
	 * <p>获取所有任务列表</p>
	 * 
	 * @return 所有任务列表
	 */
	public List<TaskEntity> allTask() {
		return new ArrayList<>(this.taskEntities);
	}
	
	/**
	 * <p>获取所有配置列表</p>
	 * 
	 * @return 所有配置列表
	 */
	public List<ConfigEntity> allConfig() {
		return new ArrayList<>(this.configEntities);
	}
	
	/**
	 * <p>设置保存信息</p>
	 * 
	 * @param entity 实体
	 */
	private void buildSave(Entity entity) {
		EntityException.requireNotNull(entity);
		EntityException.requireNull(entity.getId());
		entity.setId(UUID.randomUUID().toString());
		entity.setCreateDate(new Date());
		entity.setModifyDate(new Date());
		LOGGER.debug("保存实体：{}", entity);
	}
	
	/**
	 * <p>设置更新信息</p>
	 * 
	 * @param entity 实体
	 */
	private void buildUpdate(Entity entity) {
		EntityException.requireNotNull(entity);
		EntityException.requireNotNull(entity.getId());
		entity.setModifyDate(new Date());
		LOGGER.debug("更新实体：{}", entity);
	}
	
	/**
	 * <p>保存任务</p>
	 * 
	 * @param entity 任务
	 */
	public void save(TaskEntity entity) {
		this.buildSave(entity);
		synchronized (this) {
			this.taskEntities.add(entity);
		}
		this.persistent();
	}

	/**
	 * <p>更新任务</p>
	 * 
	 * @param entity 任务
	 */
	public void update(TaskEntity entity) {
		this.buildUpdate(entity);
		this.persistent();
	}
	
	/**
	 * <p>删除任务</p>
	 * 
	 * @param entity 任务
	 * 
	 * @return 是否删除成功
	 */
	public boolean delete(TaskEntity entity) {
		EntityException.requireNotNull(entity);
		LOGGER.debug("删除任务：{}", entity);
		if(DownloadConfig.getDelete()) {
			// 删除文件
			final var file = entity.getFile();
			if(!RecycleContext.recycle(file)) {
				FileUtils.delete(file);
			}
		}
		return this.delete(entity.getId());
	}
	
	/**
	 * <p>保存配置</p>
	 * 
	 * @param entity 配置
	 */
	public void save(ConfigEntity entity) {
		this.buildSave(entity);
		synchronized (this) {
			this.configEntities.add(entity);
		}
		this.persistent();
	}

	/**
	 * <p>更新配置</p>
	 * 
	 * @param entity 配置
	 */
	public void update(ConfigEntity entity) {
		this.buildUpdate(entity);
		this.persistent();
	}
	
	/**
	 * <p>删除配置</p>
	 * 
	 * @param entity 配置
	 * 
	 * @return 是否删除成功
	 */
	public boolean delete(ConfigEntity entity) {
		EntityException.requireNotNull(entity);
		return this.delete(entity.getId());
	}
	
	/**
	 * <p>根据配置名称查询配置</p>
	 * 
	 * @param name 配置名称
	 * 
	 * @return 配置
	 */
	public ConfigEntity findConfig(String name) {
		synchronized (this) {
			return this.configEntities.stream()
				.filter(entity -> entity.getName().equals(name))
				.findFirst()
				.orElse(null);
		}
	}
	
	/**
	 * <p>根据配置名称合并配置</p>
	 * <p>配置存在更新反之新建</p>
	 * 
	 * @param name 配置名称
	 * @param value 配置值
	 */
	public void mergeConfig(String name, String value) {
		ConfigEntity entity = this.findConfig(name);
		if(entity == null) {
			entity = new ConfigEntity();
			entity.setName(name);
			entity.setValue(value);
			this.save(entity);
		} else {
			entity.setValue(value);
			this.update(entity);
		}
	}
	
	/**
	 * <p>删除实体</p>
	 * 
	 * @param id 实体ID
	 * 
	 * @return 是否删除成功
	 */
	private boolean delete(String id) {
		LOGGER.debug("删除实体：{}", id);
		boolean success = false;
		synchronized (this) {
			// 使用或者判断：任务删除成功不再删除配置
			success =
				this.taskEntities.removeIf(entity -> entity.getId().equals(id)) ||
				this.configEntities.removeIf(entity -> entity.getId().equals(id));
		}
		if(success) {
			// 删除成功保存实体
			this.persistent();
		}
		return success;
	}
	
	/**
	 * <p>加载实体</p>
	 */
	public void load() {
		final File file = new File(ENTITY_FILE_PATH);
		if(!file.exists()) {
			return;
		}
		try (ObjectInput input = new ObjectInputStream(new FileInputStream(file))) {
			final List<?> list = (List<?>) input.readObject();
			synchronized (this) {
				this.taskEntities.clear();
				this.configEntities.clear();
				list.forEach(object -> {
					if(object instanceof TaskEntity entity) {
						this.taskEntities.add(entity);
					} else if(object instanceof ConfigEntity entity) {
						this.configEntities.add(entity);
					} else {
						LOGGER.warn("未知实体类型：{}", object);
					}
				});
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug("加载任务实体数量：{}", this.taskEntities.size());
					LOGGER.debug("加载配置实体数量：{}", this.configEntities.size());
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			LOGGER.error("加载实体异常", e);
		}
	}
	
	/**
	 * <p>保存实体</p>
	 */
	public void persistent() {
		final List<Entity> list = new ArrayList<>();
		synchronized (this) {
			list.addAll(this.taskEntities);
			list.addAll(this.configEntities);
		}
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("保存实体：{}", list.size());
		}
		final File file = new File(ENTITY_FILE_PATH);
		FileUtils.buildParentFolder(file);
		try (ObjectOutput output = new ObjectOutputStream(new FileOutputStream(file))) {
			output.writeObject(list);
		} catch (IOException e) {
			LOGGER.error("保存实体异常", e);
		}
	}
	
}
