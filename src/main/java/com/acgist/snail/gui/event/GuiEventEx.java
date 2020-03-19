package com.acgist.snail.gui.event;

import com.acgist.snail.gui.GuiManager;
import com.acgist.snail.gui.GuiManager.Mode;

/**
 * <p>GUI事件扩展</p>
 * <p>处理变长参数GUI事件</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public abstract class GuiEventEx extends GuiEvent {

	protected GuiEventEx(Type type, String name) {
		super(type, name);
	}
	
	@Override
	protected void executeNative(Object ... args) {
		executeEx(Mode.NATIVE, args);
	}

	@Override
	protected void executeExtend(Object ... args) {
		executeEx(Mode.EXTEND, args);
	}
	
	/**
	 * <p>执行变长参数GUI事件</p>
	 * 
	 * @param mode 运行模式
	 * @param args 变长参数
	 */
	protected abstract void executeEx(GuiManager.Mode mode, Object ... args);

}
