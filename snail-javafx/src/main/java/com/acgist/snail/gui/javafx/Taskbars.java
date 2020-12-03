package com.acgist.snail.gui.javafx;

import java.awt.Taskbar;
import java.awt.Window;

/**
 * <p>任务状态栏工具</p>
 * 
 * @author acgist
 */
public final class Taskbars {

	/**
	 * <p>窗口</p>
	 */
	private final Window window;
	/**
	 * <p>任务状态栏</p>
	 */
	private final Taskbar taskbar;

	public Taskbars() {
		this(null, null);
	}

	/**
	 * @param taskbar 任务状态栏
	 */
	private Taskbars(Window window, Taskbar taskbar) {
		this.window = window;
		this.taskbar = taskbar;
	}
	
	/**
	 * <p>创建工具</p>
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
	 * <p>设置进度</p>
	 * 
	 * @param value 进度
	 */
	public void progress(int value) {
		if(this.taskbar != null) {
			if(this.taskbar.isSupported(Taskbar.Feature.PROGRESS_VALUE)) {
				this.taskbar.setProgressValue(value);
			} else if(this.taskbar.isSupported(Taskbar.Feature.PROGRESS_VALUE_WINDOW)) {
				this.taskbar.setWindowProgressValue(this.window, value);
				// OFF：关闭
				// ERROR：失败（红色）
				// NORMAL：正常
				// PAUSED：暂停（黄色）
				// INDETERMINATE：未知（未知进度）
				this.taskbar.setWindowProgressState(this.window, Taskbar.State.NORMAL);
			}
		}
	}
	
	/**
	 * <p>暂停</p>
	 */
	public void paused() {
		if(this.taskbar != null) {
			if(this.taskbar.isSupported(Taskbar.Feature.PROGRESS_VALUE)) {
				this.taskbar.setProgressValue(0);
			} else if(this.taskbar.isSupported(Taskbar.Feature.PROGRESS_VALUE_WINDOW)) {
				this.taskbar.setWindowProgressState(this.window, Taskbar.State.PAUSED);
			}
		}
	}

	/**
	 * <p>失败</p>
	 */
	public void error() {
		if(this.taskbar != null) {
			if(this.taskbar.isSupported(Taskbar.Feature.PROGRESS_VALUE)) {
				this.taskbar.setProgressValue(0);
			} else if(this.taskbar.isSupported(Taskbar.Feature.PROGRESS_VALUE_WINDOW)) {
				this.taskbar.setWindowProgressState(this.window, Taskbar.State.ERROR);
			}
		}
	}

	/**
	 * <p>关闭</p>
	 */
	public void stop() {
		if(this.taskbar != null) {
			if(this.taskbar.isSupported(Taskbar.Feature.PROGRESS_VALUE)) {
				this.taskbar.setProgressValue(0);
			} else if(this.taskbar.isSupported(Taskbar.Feature.PROGRESS_VALUE_WINDOW)) {
				this.taskbar.setWindowProgressState(this.window, Taskbar.State.OFF);
			}
		}
	}
	
}
