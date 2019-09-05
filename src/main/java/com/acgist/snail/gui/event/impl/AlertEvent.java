package com.acgist.snail.gui.event.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.Alerts;
import com.acgist.snail.gui.GuiHandler.SnailAlertType;
import com.acgist.snail.gui.event.GuiEvent;

/**
 * GUI提示窗口事件
 * 
 * @author acgist
 * @since 1.1.0
 */
public class AlertEvent extends GuiEvent {

	private static final Logger LOGGER = LoggerFactory.getLogger(AlertEvent.class);
	
	protected AlertEvent() {
		super(Type.alert, "提示窗口事件");
	}

	@Override
	protected void executeNative(Object ... args) {
		SnailAlertType type;
		String title, message;
		if(args == null) {
			LOGGER.debug("提示窗口错误，参数为空。");
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
			LOGGER.debug("提示窗口错误，长度错误：{}", args.length);
			return;
		}
		Alerts.build(title, message, type.getAlertType());
	}

	@Override
	protected void executeExtend(Object ... args) {
		// TODO：外部
	}

	public static final GuiEvent newInstance() {
		return new AlertEvent();
	}

}
