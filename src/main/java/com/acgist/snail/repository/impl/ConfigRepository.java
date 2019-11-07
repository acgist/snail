package com.acgist.snail.repository.impl;

import com.acgist.snail.pojo.entity.ConfigEntity;
import com.acgist.snail.repository.Repository;

/**
 * 配置
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class ConfigRepository extends Repository<ConfigEntity> {

	public ConfigRepository() {
		super(ConfigEntity.TABLE_NAME, ConfigEntity.class);
	}

	/**
	 * 根据配置名称查询配置
	 * 
	 * @param name 配置名称
	 * 
	 * @return 配置
	 */
	public ConfigEntity findName(String name) {
		return findOne(ConfigEntity.PROPERTY_NAME, name);
	}
	
	/**
	 * 根据配置名称删除配置
	 * 
	 * @param name 配置名称
	 * 
	 * @return 删除结果：true-成功；false-配置不存在；
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
	 * 	<dt>更新配置</dt>
	 * 	<dd>不存在保存</dd>
	 * 	<dd>存在更新</dd>
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
