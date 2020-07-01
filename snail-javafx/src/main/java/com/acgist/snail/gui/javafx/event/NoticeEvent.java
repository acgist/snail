package com.acgist.snail.gui.javafx.event;

import com.acgist.snail.gui.GuiManager.SnailNoticeType;
import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.event.adapter.NoticeEventAdapter;
import com.acgist.snail.gui.javafx.menu.TrayMenu;

/**
 * <p>GUI提示消息事件</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class NoticeEvent extends NoticeEventAdapter {

	private static final NoticeEvent INSTANCE = new NoticeEvent();
	
	public static final GuiEvent getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>本地提示消息</p>
	 * 
	 * @param type 类型
	 * @param title 标题
	 * @param message 消息
	 */
	@Override
	protected void executeNativeExtend(SnailNoticeType type, String title, String message) {
		TrayMenu.getInstance().notice(title, message, type);
	}

}
