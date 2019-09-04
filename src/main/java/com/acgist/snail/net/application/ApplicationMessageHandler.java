package com.acgist.snail.net.application;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.main.MainWindow;
import com.acgist.snail.net.TcpMessageHandler;
import com.acgist.snail.pojo.message.ApplicationMessage;
import com.acgist.snail.system.context.SystemContext;
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
				onCommand(command);
				content = content.substring(index + SPLIT.length());
				index = content.indexOf(SPLIT);
			}
		}
	};
	
	/**
	 * 单条消息处理
	 */
	private void onCommand(String content) {
		content = content.trim();
		if(StringUtils.isEmpty(content)) {
			LOGGER.warn("读取系统消息内容为空");
			return;
		}
		final ApplicationMessage message = ApplicationMessage.valueOf(content);
		if(message == null) {
			LOGGER.warn("读取系统消息格式错误：{}", content);
			return;
		}
		LOGGER.debug("处理系统消息：{}", content);
		this.execute(message);
	}
	
	/**
	 * <p>处理消息</p>
	 * <p>TODO：任务：新建、列表、删除</p>
	 */
	private void execute(ApplicationMessage message) {
		if(message.getType() == null) {
			LOGGER.warn("系统消息类型为空：{}", message.getType());
			return;
		}
		switch (message.getType()) {
		case text:
			onText(message);
			break;
		case close:
			onClose(message);
			break;
		case notify:
			onNotify(message);
			break;
		case shutdown:
			onShutdown(message);
			break;
		case response:
			onResponse(message);
			break;
		default:
			LOGGER.warn("未适配的消息类型：{}", message.getType());
			break;
		}
	}

	/**
	 * 文本消息，原因返回
	 */
	private void onText(ApplicationMessage message) {
		send(ApplicationMessage.response(message.getBody()));
	}
	
	/**
	 * 关闭Socket连接
	 */
	private void onClose(ApplicationMessage message) {
		this.close();
	}
	
	/**
	 * 唤醒窗口
	 */
	private void onNotify(ApplicationMessage message) {
		Platform.runLater(() -> {
			MainWindow.getInstance().show();
		});
	}
	
	/**
	 * 关闭程序
	 */
	private void onShutdown(ApplicationMessage message) {
		SystemContext.shutdown();
	}
	
	/**
	 * 响应
	 */
	private void onResponse(ApplicationMessage message) {
		LOGGER.debug("收到响应：{}", message.getBody());
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
