package com.acgist.snail.context.initializer;

import com.acgist.snail.config.DhtConfig;
import com.acgist.snail.context.DhtContext;
import com.acgist.snail.context.NodeContext;

/**
 * DHT初始化器
 * 
 * @author acgist
 */
public final class DhtInitializer extends Initializer {

	private DhtInitializer() {
		super("DHT");
	}

	public static final DhtInitializer newInstance() {
		return new DhtInitializer();
	}

	@Override
	protected void init() {
		DhtConfig.getInstance();
		DhtContext.getInstance();
		NodeContext.getInstance();
	}

}
