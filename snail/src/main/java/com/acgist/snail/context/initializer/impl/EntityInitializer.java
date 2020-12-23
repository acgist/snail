package com.acgist.snail.context.initializer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.EntityContext;
import com.acgist.snail.context.initializer.Initializer;

/**
 * <p>初始化实体上下文</p>
 * 
 * @author acgist
 */
public final class EntityInitializer extends Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(EntityInitializer.class);
	
	/**
	 * <p>禁止创建实例</p>
	 */
	private EntityInitializer() {
	}
	
	public static final EntityInitializer newInstance() {
		return new EntityInitializer();
	}
	
	@Override
	protected void init() {
		LOGGER.info("初始化实体上下文");
		EntityContext.getInstance().load();
	}
	
}
