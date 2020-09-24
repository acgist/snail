package com.acgist.snail.gui.event.adapter;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.format.BEncodeEncoder;
import com.acgist.snail.gui.GuiManager;
import com.acgist.snail.gui.GuiManager.Mode;
import com.acgist.snail.gui.event.GuiEventExtend;
import com.acgist.snail.pojo.message.ApplicationMessage;

/**
 * <p>GUI提示窗口事件</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class AlertEventAdapter extends GuiEventExtend {

	private static final Logger LOGGER = LoggerFactory.getLogger(AlertEventAdapter.class);
	
	protected AlertEventAdapter() {
		super(Type.ALERT, "提示窗口事件");
	}

	@Override
	protected final void executeExtend(GuiManager.Mode mode, Object ... args) {
		String title;
		String message;
		GuiManager.MessageType type;
		if(args == null) {
			LOGGER.warn("提示窗口错误（参数错误）：{}", args);
			return;
		} else if(args.length == 2) {
			title = (String) args[0];
			message = (String) args[1];
			type = GuiManager.MessageType.INFO;
		} else if(args.length == 3) {
			title = (String) args[0];
			message = (String) args[1];
			type = (GuiManager.MessageType) args[2];
		} else {
			LOGGER.warn("提示窗口错误（参数长度错误）：{}", args.length);
			return;
		}
		if(mode == Mode.NATIVE) {
			this.executeNativeExtend(type, title, message);
		} else {
			this.executeExtendExtend(type, title, message);
		}
	}

	/**
	 * <p>本地提示窗口</p>
	 * 
	 * @param type 类型
	 * @param title 标题
	 * @param message 消息
	 */
	protected void executeNativeExtend(GuiManager.MessageType type, String title, String message) {
		this.executeExtendExtend(type, title, message);
	}
	
	/**
	 * <p>扩展提示窗口</p>
	 * 
	 * @param type 类型
	 * @param title 标题
	 * @param message 消息
	 */
	protected void executeExtendExtend(GuiManager.MessageType type, String title, String message) {
		final ApplicationMessage applicationMessage = ApplicationMessage.message(ApplicationMessage.Type.ALERT);
		final Map<String, String> map = new HashMap<>(5);
		map.put("type", type.name());
		map.put("title", title);
		map.put("message", message);
		final String body = BEncodeEncoder.encodeMapString(map);
		applicationMessage.setBody(body);
		GuiManager.getInstance().sendExtendGuiMessage(applicationMessage);
	}

}
