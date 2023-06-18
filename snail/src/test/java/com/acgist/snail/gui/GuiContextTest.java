package com.acgist.snail.gui;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.acgist.snail.Snail;
import com.acgist.snail.context.SystemContext;
import com.acgist.snail.format.BEncodeEncoder;
import com.acgist.snail.gui.GuiContext.MessageType;
import com.acgist.snail.gui.event.GuiEventMessage;
import com.acgist.snail.net.application.ApplicationClient;
import com.acgist.snail.net.application.ApplicationMessage.Type;
import com.acgist.snail.utils.Performance;

class GuiContextTest extends Performance {

	/**
	 * <p>测试环境客户端和服务端使用同一套事件</p>
	 * <p>客户端收到事件也会发出通知所以会提示：发送扩展GUI消息失败</p>
	 */
	@BeforeAll
	static final void registerEvent() {
		GuiContext.registerAdapter();
	}

	@Test
	void testEvent() {
		final var guiContext = GuiContext.getInstance();
		guiContext.alert("acgist", "acgist");
		guiContext.notice("acgist", "acgist");
		guiContext.show();
		guiContext.hide();
		guiContext.alert("acgist", "acgist");
		guiContext.alert("acgist", "acgist", MessageType.ERROR);
		guiContext.notice("acgist", "acgist");
		guiContext.notice("acgist", "acgist", MessageType.ERROR);
		guiContext.refreshTaskList();
		guiContext.refreshTaskStatus();
		assertNotNull(guiContext);
	}
	
	@Test
	void testExtend() {
		LOGGER.info("系统开始启动");
		SystemContext.info();
		SystemContext.build(); // 初始化系统上下文
		assertTrue(Snail.available());
		if(Snail.available()) {
			final String[] args = new String[] { "mode=extend" }; // 后台模式启动
			GuiContext.getInstance().init(args).build(); // 初始化GUI
		} else {
			LOGGER.debug("启动系统失败");
		}
		LOGGER.info("系统启动完成");
	}
	
	@Test
	void testSocket() {
		String message = null;
		final Scanner scanner = new Scanner(System.in);
		final ApplicationClient client = ApplicationClient.newInstance();
		client.connect();
		// 注册GUI
		client.send(Type.GUI.build(message));
		assertNotNull(client);
		while ((message = scanner.nextLine()) != null) {
			if(message.equalsIgnoreCase(Type.TEXT.name())) {
				client.send(Type.TEXT.build(message));
			} else if(message.equalsIgnoreCase(Type.CLOSE.name())) {
				client.send(Type.CLOSE.build(message));
				client.close();
				break;
			} else if(message.equalsIgnoreCase(Type.NOTIFY.name())) {
				client.send(Type.NOTIFY.build(message));
			} else if(message.equalsIgnoreCase(Type.ALERT.name())) {
				final Map<String, String> map = Map.of(
					GuiEventMessage.MESSAGE_TYPE, GuiContext.MessageType.INFO.name(),
					GuiEventMessage.MESSAGE_TITLE, "测试",
					GuiEventMessage.MESSAGE_MESSAGE, message
				);
				final String body = BEncodeEncoder.encodeMapString(map);
				client.send(Type.ALERT.build(body));
			} else if(message.equalsIgnoreCase(Type.NOTICE.name())) {
				final Map<String, String> map = Map.of(
					GuiEventMessage.MESSAGE_TYPE, GuiContext.MessageType.INFO.name(),
					GuiEventMessage.MESSAGE_TITLE, "测试",
					GuiEventMessage.MESSAGE_MESSAGE, message
				);
				final String body = BEncodeEncoder.encodeMapString(map);
				client.send(Type.NOTICE.build(body));
			} else if(message.equalsIgnoreCase(Type.SHUTDOWN.name())) {
				client.send(Type.SHUTDOWN.build(message));
				client.close();
				break;
			} else if(message.equalsIgnoreCase(Type.TASK_NEW.name())) {
				final Map<String, String> map = new HashMap<>();
//				map.put("url", "下载地址或者种子文件路径");
				// BT任务
//				map.put("files", "B编码下载文件列表");
				map.put("url", "https://mirrors.bfsu.edu.cn/apache/tomcat/tomcat-10/v10.0.4/bin/apache-tomcat-10.0.4.zip");
				client.send(Type.TASK_NEW.build(BEncodeEncoder.encodeMapString(map)));
			} else if(message.equalsIgnoreCase(Type.TASK_LIST.name())) {
				client.send(Type.TASK_LIST.build(message));
			} else if(message.equalsIgnoreCase(Type.TASK_START.name())) {
				client.send(Type.TASK_START.build("37f48162-d306-4fff-b161-f1231a3f7e48"));
			} else if(message.equalsIgnoreCase(Type.TASK_PAUSE.name())) {
				client.send(Type.TASK_PAUSE.build("37f48162-d306-4fff-b161-f1231a3f7e48"));
			} else if(message.equalsIgnoreCase(Type.TASK_DELETE.name())) {
				client.send(Type.TASK_DELETE.build("37f48162-d306-4fff-b161-f1231a3f7e48"));
			} else {
				client.send(Type.TEXT.build(message));
			}
		}
		scanner.close();
	}
	
}
