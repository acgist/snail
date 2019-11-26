package com.acgist.snail.gui.event.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.GuiHandler;
import com.acgist.snail.gui.GuiHandler.SnailNoticeType;
import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.menu.TrayMenu;
import com.acgist.snail.pojo.message.ApplicationMessage;
import com.acgist.snail.system.bencode.BEncodeEncoder;

/**
 * GUI提示消息事件
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class NoticeEvent extends GuiEvent {

	private static final Logger LOGGER = LoggerFactory.getLogger(NoticeEvent.class);
	
	private static final NoticeEvent INSTANCE = new NoticeEvent();
	
	protected NoticeEvent() {
		super(Type.NOTICE, "提示消息事件");
	}
	
	public static final GuiEvent getInstance() {
		return INSTANCE;
	}
	
	@Override
	protected void executeNative(Object ... args) {
		executeEx(true, args);
	}

	@Override
	protected void executeExtend(Object ... args) {
		executeEx(false, args);
	}

	private void executeEx(boolean gui, Object ... args) {
		SnailNoticeType type;
		String title, message;
		if(args == null) {
			LOGGER.warn("提示消息错误（参数错误）：{}", args);
			return;
		} else if(args.length == 2) {
			title = (String) args[0];
			message = (String) args[1];
			type = SnailNoticeType.INFO;
		} else if(args.length == 3) {
			title = (String) args[0];
			message = (String) args[1];
			type = (SnailNoticeType) args[2];
		} else {
			LOGGER.warn("提示消息错误（参数长度错误）：{}", args.length);
			return;
		}
		if(gui) {
			executeNativeEx(title, message, type);
		} else {
			executeExtendEx(title, message, type);
		}
	}
	
	private void executeNativeEx(String title, String message, SnailNoticeType type) {
		TrayMenu.getInstance().notice(title, message, type.getMessageType());
	}
	
	private void executeExtendEx(String title, String message, SnailNoticeType type) {
		final ApplicationMessage applicationMessage = ApplicationMessage.message(ApplicationMessage.Type.NOTICE);
		final Map<String, String> map = new HashMap<>(3);
		map.put("type", type.name());
		map.put("title", title);
		map.put("message", message);
		final String body = BEncodeEncoder.encodeMapString(map);
		applicationMessage.setBody(body);
		GuiHandler.getInstance().sendExtendGuiMessage(applicationMessage);
	}

}
