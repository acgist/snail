package com.acgist.snail.gui.event;

import com.acgist.snail.gui.GuiContext;
import com.acgist.snail.gui.GuiContext.Mode;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;

/**
 * GUI变长参数事件
 * 
 * @author acgist
 */
public abstract class GuiEventArgs extends GuiEvent {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GuiEventArgs.class);

    /**
     * @param type 事件类型
     * @param name 事件名称
     */
    protected GuiEventArgs(Type type, String name) {
        super(type, name);
    }
    
    @Override
    protected final void executeNative(Object ... args) {
        this.executeExtend(Mode.NATIVE, args);
    }

    @Override
    protected final void executeExtend(Object ... args) {
        this.executeExtend(Mode.EXTEND, args);
    }
    
    /**
     * 校验参数
     * 
     * @param args   参数
     * @param length 参数长度
     * 
     * @return 是否校验成功
     */
    protected final boolean check(Object[] args, int length) {
        return this.check(args, length, length);
    }
    
    /**
     * 校验参数
     * 
     * @param args      参数
     * @param minLength 最小参数长度
     * @param maxLength 最大参数长度
     * 
     * @return 是否校验成功
     */
    protected final boolean check(Object[] args, int minLength, int maxLength) {
        if(args == null) {
            LOGGER.warn("参数格式错误（为空）：{} - {}", this.name, args);
            return false;
        }
        if(args.length < minLength || args.length > maxLength) {
            LOGGER.warn("参数格式错误（长度）：{} - {}", this.name, args);
            return false;
        }
        return true;
    }
    
    /**
     * @param args  参数
     * @param index 参数序号
     * 
     * @return 参数
     */
    protected final Object getArg(Object[] args, int index) {
        return this.getArg(args, index, null);
    }
    
    /**
     * @param args         参数
     * @param index        参数序号
     * @param defaultValue 默认值
     * 
     * @return 参数
     */
    protected final Object getArg(Object[] args, int index, Object defaultValue) {
        if(args.length > index) {
            final Object arg = args[index];
            return arg == null ? defaultValue : arg;
        } else {
            return defaultValue;
        }
    }
    
    /**
     * 执行变长参数GUI事件
     * 
     * @param mode 运行模式
     * @param args 变长参数
     */
    protected abstract void executeExtend(GuiContext.Mode mode, Object ... args);

}
