package com.acgist.snail.context;

/**
 * 实体初始化器
 * 
 * @author acgist
 */
public final class EntityInitializer extends Initializer {

	private EntityInitializer() {
		super("实体");
	}
	
	public static final EntityInitializer newInstance() {
		return new EntityInitializer();
	}
	
	@Override
	protected void init() {
		EntityContext.getInstance().load();
	}
	
	@Override
	protected void release() {
		EntityContext.getInstance().persistent();
	}
	
}
