package com.acgist.snail.gui.event.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.GuiHandler.SnailNoticeType;
import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.menu.TrayMenu;

/**
 * GUI提示消息事件
 * 
 * @author acgist
 * @since 1.1.0
 */
public class NoticeEvent extends GuiEvent {

	private static final Logger LOGGER = LoggerFactory.getLogger(NoticeEvent.class);
	
	protected NoticeEvent() {
		super(Type.notice, "提示消息事件");
	}

	@Override
	protected void executeNative(Object ... args) {
		SnailNoticeType type;
		String title, message;
		if(args == null) {
			LOGGER.debug("提示消息错误，参数为空。");
			return;
		} else if(args.length == 2) {
			title = (String) args[0];
			message = (String) args[1];
			type = SnailNoticeType.info;
		} else if(args.length == 3) {
			title = (String) args[0];
			message = (String) args[1];
			type = (SnailNoticeType) args[2];
		} else {
			LOGGER.debug("提示消息错误，长度错误：{}", args.length);
			return;
		}
		TrayMenu.getInstance().notice(title, message, type.getMessageType());
	}

	@Override
	protected void executeExtend(Object ... args) {
		// TODO：外部
	}

	public static final GuiEvent newInstance() {
		return new NoticeEvent();
	}

}
