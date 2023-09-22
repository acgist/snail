package com.acgist.snail.gui.event;

import com.acgist.snail.gui.GuiContext;
import com.acgist.snail.gui.GuiContext.Mode;
import com.acgist.snail.net.application.ApplicationMessage;

/**
 * GUI事件
 * 
 * @author acgist
 */
public abstract class GuiEvent {

    /**
     * GUI事件类型
     * 
     * @author acgist
     */
    public enum Type {
        
        /**
         * 显示窗口
         */
        SHOW,
        /**
         * 隐藏窗口
         */
        HIDE,
        /**
         * 退出窗口
         */
        EXIT,
        /**
         * 新建窗口
         */
        BUILD,
        /**
         * 窗口消息
         */
        ALERT,
        /**
         * 提示消息
         */
        NOTICE,
        /**
         * 选择下载文件
         * 多文件下载任务选择下载文件
         */
        MULTIFILE,
        /**
         * 刷新任务列表：添加、删除
         */
        REFRESH_TASK_LIST,
        /**
         * 刷新任务状态：开始、暂停、完成
         */
        REFRESH_TASK_STATUS,
        /**
         * 响应消息
         */
        RESPONSE;
        
    }

    /**
     * 事件类型
     */
    protected final Type type;
    /**
     * 事件名称
     */
    protected final String name;
    
    /**
     * GUI事件
     * 
     * @param type 事件类型
     * @param name 事件名称
     */
    protected GuiEvent(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    /**
     * 执行GUI事件
     * 
     * @param mode 运行模式
     * @param args 参数
     */
    public final void execute(GuiContext.Mode mode, Object ... args) {
        if(mode == Mode.NATIVE) {
            this.executeNative(args);
        } else {
            this.executeExtend(args);
        }
    }
    
    /**
     * 通知扩展消息
     * 
     * @param message 扩展消息
     */
    protected void sendExtendGuiMessage(ApplicationMessage message) {
        GuiContext.getInstance().sendExtendGuiMessage(message);
    }
    
    /**
     * 执行本地GUI事件
     * 
     * @param args 参数
     */
    protected abstract void executeNative(Object ... args);
    
    /**
     * 执行扩展GUI事件
     * 
     * @param args 参数
     */
    protected abstract void executeExtend(Object ... args);
    
    /**
     * @return 事件类型
     */
    public Type getType() {
        return this.type;
    }
    
    /**
     * @return 事件名称
     */
    public String getName() {
        return this.name;
    }
    
}
