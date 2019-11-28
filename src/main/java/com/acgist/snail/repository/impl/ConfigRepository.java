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
		return findOne(ConfigEntity.PROPERTY_NAME, name);
	}
	
	/**
	 * <p>根据配置名称删除配置</p>
	 * 
	 * @param name 配置名称
	 * 
	 * @return 删除结果：true-成功；false-失败（配置不存在）；
	 */
	public boolean deleteName(String name) {
		final ConfigEntity entity = findOne(ConfigEntity.PROPERTY_NAME, name);
		if(entity != null) {
			delete(entity.getId());
			return true;
		}
		return false;
	}
	
	/**
	 * <dl>
	 * 	<dt>合并配置</dt>
	 * 	<dd>存在：更新</dd>
	 * 	<dd>不存在：保存</dd>
	 * </dl>
	 */
	public void merge(String name, String value) {
		ConfigEntity entity = findOne(ConfigEntity.PROPERTY_NAME, name);
		if(entity == null) {
			entity = new ConfigEntity();
			entity.setName(name);
			entity.setValue(value);
			save(entity);
		} else {
			entity.setValue(value);
			update(entity);
		}
	}

}
