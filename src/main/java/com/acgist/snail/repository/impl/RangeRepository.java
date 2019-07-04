package com.acgist.snail.repository.impl;

import com.acgist.snail.pojo.entity.RangeEntity;
import com.acgist.snail.repository.Repository;

/**
 * IP范围
 * 
 * @author acgist
 * @since 1.1.0
 */
public class RangeRepository extends Repository<RangeEntity> {
	
//	private static final Logger LOGGER = LoggerFactory.getLogger(RangeRepository.class);

	public RangeRepository() {
		super(RangeEntity.TABLE_NAME);
	}

}
