package com.acgist.snail.context;

/**
 * 定时任务时间周期异常
 * 
 * @author acgist
 */
public final class ScheduledException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public ScheduledException() {
        super("定时任务时间周期异常");
    }
    
    /**
     * @param duration 时间周期
     */
    public ScheduledException(long duration) {
        super("定时任务时间周期错误：" + duration);
    }

    /**
     * @param message 错误信息
     */
    public ScheduledException(String message) {
        super(message);
    }

    /**
     * @param cause 原始异常
     */
    public ScheduledException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message 错误信息
     * @param cause   原始异常
     */
    public ScheduledException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 验证定时任务时间周期
     * 
     * @param duration 时间周期
     */
    public static final void verify(long duration) {
        if(duration < 0) {
            throw new ScheduledException(duration);
        }
    }
    
}
