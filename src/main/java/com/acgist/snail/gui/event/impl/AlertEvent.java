package com.acgist.snail.gui.event.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.Alerts;
import com.acgist.snail.gui.GuiHandler;
import com.acgist.snail.gui.GuiHandler.SnailAlertType;
import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.pojo.message.ApplicationMessage;
import com.acgist.snail.system.bencode.BEncodeEncoder;

/**
 * GUI提示窗口事件
 * 
 * @author acgist
 * @since 1.1.0
 */
public class AlertEvent extends GuiEvent {

	private static final Logger LOGGER = LoggerFactory.getLogger(AlertEvent.class);
	
	private static final AlertEvent INSTANCE = new AlertEvent();
	
	protected AlertEvent() {
		super(Type.alert, "提示窗口事件");
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
		SnailAlertType type;
		String title, message;
		if(args == null) {
			LOGGER.warn("提示窗口错误，参数错误：{}", args);
			return;
		} else if(args.length == 2) {
			title = (String) args[0];
			message = (String) args[1];
			type = SnailAlertType.info;
		} else if(args.length == 3) {
			title = (String) args[0];
			message = (String) args[1];
			type = (SnailAlertType) args[2];
		} else {
			LOGGER.warn("提示窗口错误，参数错误（长度）：{}", args.length);
			return;
		}
		if(gui) {
			executeNativeEx(type, title, message);
		} else {
			executeExtendEx(type, title, message);
		}
	}

	private void executeNativeEx(SnailAlertType type, String title, String message) {
		Alerts.build(title, message, type.getAlertType());
	}
	
	private void executeExtendEx(SnailAlertType type, String title, String message) {
		final ApplicationMessage applicationMessage = ApplicationMessage.message(ApplicationMessage.Type.alert);
		final Map<String, String> map = new HashMap<>(3);
		map.put("title", title);
		map.put("message", message);
		map.put("type", type.name());
		final String body = BEncodeEncoder.encodeMapString(map);
		applicationMessage.setBody(body);
		GuiHandler.getInstance().sendGuiMessage(applicationMessage);
	}

}
