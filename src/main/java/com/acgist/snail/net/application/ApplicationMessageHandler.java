package com.acgist.snail.net.application;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.main.MainWindow;
import com.acgist.snail.net.TcpMessageHandler;
import com.acgist.snail.pojo.message.ApplicationMessage;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.StringUtils;

import javafx.application.Platform;

/**
 * 系统消息
 * 
 * @author acgist
 * @since 1.0.0
 */
public class ApplicationMessageHandler extends TcpMessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationMessageHandler.class);
	
	public static final String SPLIT = "\r\n"; // 处理粘包分隔符
	
	public ApplicationMessageHandler() {
		super(SPLIT);
	}
	
	@Override
	public void onMessage(ByteBuffer attachment) throws NetException {
		String command;
		String content = IoUtils.readContent(attachment);
		if(content.contains(SPLIT)) {
			int index = content.indexOf(SPLIT);
			while(index >= 0) {
				command = content.substring(0, index);
				oneMessage(command);
				content = content.substring(index + SPLIT.length());
				index = content.indexOf(SPLIT);
			}
		}
	};
	
	/**
	 * 单条消息处理
	 */
	private void oneMessage(String json) {
		json = json.trim();
		if(StringUtils.isEmpty(json)) {
			LOGGER.warn("读取消息内容为空");
			return;
		}
		ApplicationMessage message = ApplicationMessage.valueOf(json);
		if(message == null) {
			LOGGER.warn("读取消息格式错误：{}", json);
			return;
		}
		LOGGER.info("读取消息：{}", json);
		this.execute(message);
	}
	
	/**
	 * 处理消息
	 */
	private void execute(ApplicationMessage message) {
		if(message.getType() == ApplicationMessage.Type.text) { // 文本信息：直接原样返回
			send(ApplicationMessage.response(message.getBody()));
		} else if(message.getType() == ApplicationMessage.Type.notify) { // 唤醒主窗口
			Platform.runLater(() -> {
				MainWindow.getInstance().show();
			});
		} else if(message.getType() == ApplicationMessage.Type.close) { // 关闭
			this.close();
		} else if(message.getType() == ApplicationMessage.Type.response) { // 响应内容
			LOGGER.info("收到响应：{}", message.getBody());
		} else {
			LOGGER.warn("未适配的消息类型：{}", message.getType());
		}
	}

	/**
	 * 发送消息
	 */
	private void send(ApplicationMessage message) {
		try {
			send(message.toString());
		} catch (NetException e) {
			LOGGER.error("发送Application消息异常", e);
		}
	}

}
