package com.acgist.snail.gui.event;

import com.acgist.snail.gui.GuiManager;
import com.acgist.snail.gui.GuiManager.Mode;

/**
 * <p>GUI事件扩展</p>
 * <p>处理变长参数GUI事件</p>
 * 
 * @author acgist
 */
public abstract class GuiEventExtend extends GuiEvent {

	/**
	 * <p>GUI事件扩展</p>
	 * 
	 * @param type 事件类型
	 * @param name 事件名称
	 */
	protected GuiEventExtend(Type type, String name) {
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
	 * <p>执行变长参数GUI事件</p>
	 * 
	 * @param mode 运行模式
	 * @param args 变长参数
	 */
	protected abstract void executeExtend(GuiManager.Mode mode, Object ... args);

}
