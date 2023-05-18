package com.acgist.snail.context;

/**
 * 初始化实体
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
