package com.acgist.snail.context.initializer;

import com.acgist.snail.context.NatContext;

/**
 * NAT初始化器
 * 
 * @author acgist
 */
public final class NatInitializer extends Initializer {
	
	private NatInitializer() {
		super("NAT");
	}
	
	public static final NatInitializer newInstance() {
		return new NatInitializer();
	}
	
	@Override
	protected void init() {
		NatContext.getInstance().register();
	}

}
