package com.acgist.snail.net;

import com.acgist.snail.context.Initializer;

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
