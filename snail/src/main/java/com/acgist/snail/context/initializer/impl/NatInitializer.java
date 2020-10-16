package com.acgist.snail.context.initializer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.NatContext;
import com.acgist.snail.context.initializer.Initializer;

/**
 * <p>初始化NAT</p>
 * 
 * @author acgist
 */
public final class NatInitializer extends Initializer {
	
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
