package com.acgist.snail.gui.event;

import java.util.Map;

import com.acgist.snail.format.BEncodeEncoder;
import com.acgist.snail.gui.GuiManager;
import com.acgist.snail.gui.GuiManager.MessageType;
import com.acgist.snail.gui.GuiManager.Mode;
import com.acgist.snail.pojo.message.ApplicationMessage;

/**
 * <p>GUI消息事件</p>
 * 
 * @author acgist
 */
public abstract class GuiEventMessage extends GuiEventArgs {

	/**
	 * <p>消息类型</p>
	 */
	protected final ApplicationMessage.Type messageType;
	
	/**
	 * @param type 事件类型
	 * @param name 事件名称
	 * @param messageType 消息类型
	 */
	protected GuiEventMessage(Type type, String name, ApplicationMessage.Type messageType) {
		super(type, name);
		this.messageType = messageType;
	}

	@Override
	protected final void executeExtend(GuiManager.Mode mode, Object ... args) {
		if(!this.check(args, 2, 3)) {
			return;
		}
		final String title = (String) this.getArg(args, 0);
		final String message = (String) this.getArg(args, 1);
		final GuiManager.MessageType type = (MessageType) this.getArg(args, 2, GuiManager.MessageType.INFO);
		if(mode == Mode.NATIVE) {
			this.executeNativeExtend(type, title, message);
		} else {
			this.executeExtendExtend(type, title, message);
		}
	}
	
	/**
	 * <p>本地消息</p>
	 * 
	 * @param type 类型
	 * @param title 标题
	 * @param message 消息
	 */
	protected void executeNativeExtend(GuiManager.MessageType type, String title, String message) {
		this.executeExtendExtend(type, title, message);
	}
	
	/**
	 * <p>扩展消息</p>
	 * 
	 * @param type 类型
	 * @param title 标题
	 * @param message 消息
	 */
	protected void executeExtendExtend(GuiManager.MessageType type, String title, String message) {
		final Map<String, String> map = Map.of(
			"type", type.name(),
			"title", title,
			"message", message
		);
		final String body = BEncodeEncoder.encodeMapString(map);
		final ApplicationMessage applicationMessage = ApplicationMessage.message(this.messageType, body);
		GuiManager.getInstance().sendExtendGuiMessage(applicationMessage);
	}

}
