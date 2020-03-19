package com.acgist.snail.gui.event.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.GuiManager;
import com.acgist.snail.gui.GuiManager.Mode;
import com.acgist.snail.gui.GuiManager.SnailNoticeType;
import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.event.GuiEventEx;
import com.acgist.snail.gui.menu.TrayMenu;
import com.acgist.snail.pojo.message.ApplicationMessage;
import com.acgist.snail.system.bencode.BEncodeEncoder;

/**
 * <p>GUI提示消息事件</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class NoticeEvent extends GuiEventEx {

	private static final Logger LOGGER = LoggerFactory.getLogger(NoticeEvent.class);
	
	private static final NoticeEvent INSTANCE = new NoticeEvent();
	
	protected NoticeEvent() {
		super(Type.NOTICE, "提示消息事件");
	}
	
	public static final GuiEvent getInstance() {
		return INSTANCE;
	}
	
	@Override
	protected void executeEx(GuiManager.Mode mode, Object ... args) {
		SnailNoticeType type;
		String title;
		String message;
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
		if(mode == Mode.NATIVE) {
			executeNativeEx(type, title, message);
		} else {
			executeExtendEx(type, title, message);
		}
	}
	
	/**
	 * <p>本地提示消息</p>
	 * 
	 * @param type 类型
	 * @param title 标题
	 * @param message 消息
	 */
	private void executeNativeEx(SnailNoticeType type, String title, String message) {
		TrayMenu.getInstance().notice(title, message, type.getMessageType());
	}
	
	/**
	 * <p>扩展提示消息</p>
	 * 
	 * @param type 类型
	 * @param title 标题
	 * @param message 消息
	 */
	private void executeExtendEx(SnailNoticeType type, String title, String message) {
		final ApplicationMessage applicationMessage = ApplicationMessage.message(ApplicationMessage.Type.NOTICE);
		final Map<String, String> map = new HashMap<>(5);
		map.put("type", type.name());
		map.put("title", title);
		map.put("message", message);
		final String body = BEncodeEncoder.encodeMapString(map);
		applicationMessage.setBody(body);
		GuiManager.getInstance().sendExtendGuiMessage(applicationMessage);
	}

}
