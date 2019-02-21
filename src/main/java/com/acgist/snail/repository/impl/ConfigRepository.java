package com.acgist.snail.repository.impl;

import com.acgist.snail.pojo.entity.ConfigEntity;
import com.acgist.snail.repository.BaseRepository;

/**
 * 配置
 */
public class ConfigRepository extends BaseRepository<ConfigEntity> {

	public ConfigRepository() {
		super(ConfigEntity.TABLE_NAME);
	}

}
