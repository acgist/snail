package com.acgist.snail.repository.impl;

import com.acgist.snail.pojo.entity.ConfigEntity;
import com.acgist.snail.repository.Repository;

/**
 * <p>配置</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class ConfigRepository extends Repository<ConfigEntity> {

	public ConfigRepository() {
		super(ConfigEntity.TABLE_NAME, ConfigEntity.class);
	}

	/**
	 * <p>根据配置名称查询配置</p>
	 * 
	 * @param name 配置名称
	 * 
	 * @return 配置
	 */
	public ConfigEntity findName(String name) {
		return this.findOne(ConfigEntity.PROPERTY_NAME, name);
	}
	
	/**
	 * <p>根据配置名称删除配置</p>
	 * <p>配置不存在时删除失败</p>
	 * 
	 * @param name 配置名称
	 * 
	 * @return 删除结果：true-成功；false-失败；
	 */
	public boolean deleteName(String name) {
		final ConfigEntity entity = this.findName(name);
		if(entity != null) {
			this.delete(entity.getId());
			return true;
		}
		return false;
	}
	
	/**
	 * <p>合并配置</p>
	 * <p>配置存在：更新</p>
	 * <p>配置不存在：保存</p>
	 * 
	 * @param name 配置名称
	 * @param value 配置值
	 */
	public void merge(String name, String value) {
		ConfigEntity entity = this.findName(name);
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

}
