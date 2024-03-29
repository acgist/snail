package com.acgist.snail.context;

/**
 * 初始化任务
 * 
 * @author acgist
 */
public final class TaskInitializer extends Initializer {

    private TaskInitializer() {
        super("任务");
    }
    
    public static final TaskInitializer newInstance() {
        return new TaskInitializer();
    }
    
    @Override
    protected void init() {
        TaskContext.getInstance().load();
    }

    @Override
    protected void release() {
        TaskContext.getInstance().shutdown();
    }
    
}
