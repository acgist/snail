package com.acgist.snail.system.initializer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.context.NatContext;
import com.acgist.snail.system.initializer.Initializer;

/**
 * <p>初始化NAT</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class NatInitializer extends Initializer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NatInitializer.class);
	
	private NatInitializer() {
	}
	
	public static final NatInitializer newInstance() {
		return new NatInitializer();
	}
	
	@Override
	protected void init() {
		LOGGER.info("初始化NAT");
		NatContext.getInstance().init();
	}

}
