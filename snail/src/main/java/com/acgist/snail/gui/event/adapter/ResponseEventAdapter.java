package com.acgist.snail.gui.event.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.GuiManager;
import com.acgist.snail.gui.GuiManager.Mode;
import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.event.GuiEventExtend;
import com.acgist.snail.pojo.message.ApplicationMessage;

/**
 * <p>GUI响应消息事件</p>
 * 
 * @author acgist
 * @since 1.4.0
 */
public class ResponseEventAdapter extends GuiEventExtend {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResponseEventAdapter.class);
	
	protected ResponseEventAdapter() {
		super(GuiEvent.Type.RESPONSE, "响应消息事件");
	}

	@Override
	protected final void executeExtend(Mode mode, Object... args) {
		String message;
		if(args == null) {
			LOGGER.warn("响应消息错误（参数错误）：{}", args);
			return;
		} else if(args.length == 1) {
			message = (String) args[0];
		} else {
			LOGGER.warn("响应消息错误（参数长度错误）：{}", args.length);
			return;
		}
		if(mode == Mode.NATIVE) {
			this.executeNativeExtend(message);
		} else {
			this.executeExtendExtend(message);
		}
	}

	/**
	 * <p>本地响应消息</p>
	 * 
	 * @param message 消息
	 */
	protected void executeNativeExtend(String message) {
		this.executeExtendExtend(message);
	}
	
	/**
	 * <p>扩展响应消息</p>
	 * 
	 * @param message 消息
	 */
	protected void executeExtendExtend(String message) {
		final ApplicationMessage applicationMessage = ApplicationMessage.message(ApplicationMessage.Type.RESPONSE);
		applicationMessage.setBody(message);
		GuiManager.getInstance().sendExtendGuiMessage(applicationMessage);
	}
	
}
