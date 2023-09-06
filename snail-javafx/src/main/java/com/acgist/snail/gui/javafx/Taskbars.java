package com.acgist.snail.gui.javafx;

import java.awt.Taskbar;
import java.awt.Window;

/**
 * 状态栏助手
 * 
 * TODO：JavaFX是时候才能支持呢
 * 
 * @author acgist
 */
public final class Taskbars {

    /**
     * 窗口
     */
    private final Window window;
    /**
     * 状态栏
     */
    private final Taskbar taskbar;

    private Taskbars() {
        this(null, null);
    }

    /**
     * @param window  窗口
     * @param taskbar 状态栏
     */
    private Taskbars(Window window, Taskbar taskbar) {
        this.window  = window;
        this.taskbar = taskbar;
    }
    
    /**
     * 新建状态栏助手
     * 
     * @param window 窗口
     * 
     * @return Taskbars
     */
    public static final Taskbars newInstance(Window window) {
        if(Taskbar.isTaskbarSupported()) {
            return new Taskbars(window, Taskbar.getTaskbar());
        }
        return new Taskbars();
    }

    /**
     * 设置进度
     * 
     * @param value 进度
     */
    public void progress(int value) {
        if(this.taskbar == null) {
            return;
        }
        if(this.taskbar.isSupported(Taskbar.Feature.PROGRESS_VALUE)) {
            this.taskbar.setProgressValue(value);
        } else if(this.taskbar.isSupported(Taskbar.Feature.PROGRESS_VALUE_WINDOW)) {
            // OFF           关闭
            // ERROR         失败（红色）
            // NORMAL        正常（绿色）
            // PAUSED        暂停（黄色）
            // INDETERMINATE 未知（未知进度）
            this.taskbar.setWindowProgressValue(this.window, value);
            this.taskbar.setWindowProgressState(this.window, Taskbar.State.NORMAL);
        }
    }
    
    /**
     * 暂停
     */
    public void paused() {
        if(this.taskbar == null) {
            return;
        }
        if(this.taskbar.isSupported(Taskbar.Feature.PROGRESS_VALUE)) {
            this.taskbar.setProgressValue(0);
        } else if(this.taskbar.isSupported(Taskbar.Feature.PROGRESS_VALUE_WINDOW)) {
            this.taskbar.setWindowProgressState(this.window, Taskbar.State.PAUSED);
        }
    }

    /**
     * 失败
     */
    public void error() {
        if(this.taskbar == null) {
            return;
        }
        if(this.taskbar.isSupported(Taskbar.Feature.PROGRESS_VALUE)) {
            this.taskbar.setProgressValue(0);
        } else if(this.taskbar.isSupported(Taskbar.Feature.PROGRESS_VALUE_WINDOW)) {
            this.taskbar.setWindowProgressState(this.window, Taskbar.State.ERROR);
        }
    }

    /**
     * 关闭
     */
    public void stop() {
        if(this.taskbar == null) {
            return;
        }
        if(this.taskbar.isSupported(Taskbar.Feature.PROGRESS_VALUE)) {
            this.taskbar.setProgressValue(0);
        } else if(this.taskbar.isSupported(Taskbar.Feature.PROGRESS_VALUE_WINDOW)) {
            this.taskbar.setWindowProgressState(this.window, Taskbar.State.OFF);
        }
    }
    
}
