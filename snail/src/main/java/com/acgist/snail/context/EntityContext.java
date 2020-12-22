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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.entity.ConfigEntity;
import com.acgist.snail.pojo.entity.Entity;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.IoUtils;

/**
 * <p>实体管理</p>
 * 
 * @author acgist
 * 
 * TODO：删除H2数据库
 */
public final class EntityContext {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EntityContext.class);
	
	/**
	 * <p>数据库文件名称：{@value}</p>
	 */
	private static final String ENTITY_FILE_PATH = "./database/snail.entity";
	
	private static final EntityContext INSTANCE = new EntityContext();
	
	public static final EntityContext getInstance() {
		return INSTANCE;
	}

	/**
	 * <p>任务列表</p>
	 */
	private final List<TaskEntity> taskEntities;
	/**
	 * <p>配置列表</p>
	 */
	private final List<ConfigEntity> configEntities;
	
	/**
	 * <p>禁止创建实例</p>
	 */
	private EntityContext() {
		this.taskEntities = new ArrayList<>();
		this.configEntities = new ArrayList<>();
		this.load();
	}
	
	public List<TaskEntity> allTask() {
		return this.taskEntities;
	}
	
	public List<ConfigEntity> allConfig() {
		return this.configEntities;
	}
	
	public void findTask() {
	}
	
	public void findConfig() {
	}

	public void save(TaskEntity taskEntity) {
		synchronized (this) {
			this.taskEntities.add(taskEntity);
		}
	}

	public void update(TaskEntity taskEntity) {
	}
	
	public void merge(TaskEntity taskEntity) {
	}
	
	public void save(ConfigEntity configEntity) {
	}
	
	public void update(ConfigEntity configEntity) {
	}
	
	public void merge(ConfigEntity configEntity) {
	}
	
	private void load() {
		final File file = new File(ENTITY_FILE_PATH);
		if(!file.exists()) {
			return;
		}
		ObjectInput input = null;
		try {
			input = new ObjectInputStream(new FileInputStream(file));
			final List<?> list = (List<?>) input.readObject();
			list.forEach(object -> {
				if(object instanceof TaskEntity) {
					this.taskEntities.add((TaskEntity) object);
				} else if(object instanceof ConfigEntity) {
					this.taskEntities.add((TaskEntity) object);
				} else {
					LOGGER.warn("不支持的类型：{}", object);
				}
			});
			LOGGER.debug("加载实体数量：{}-{}", this.taskEntities.size(), this.configEntities.size());
		} catch (IOException | ClassNotFoundException e) {
			LOGGER.error("加载实体异常", e);
		} finally {
			IoUtils.close(input);
		}
	}
	ObjectOutput output;

	public void persistent() {
		final List<Entity> list = new ArrayList<>();
		synchronized (this) {
			list.addAll(this.taskEntities);
			list.addAll(this.configEntities);
		}
		try {
			final File file = new File(ENTITY_FILE_PATH);
			FileUtils.buildFolder(file, true);
			if(output == null)
			output = new ObjectOutputStream(new FileOutputStream(file));
			output.writeObject(list);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
