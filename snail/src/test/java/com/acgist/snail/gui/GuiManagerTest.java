package com.acgist.snail.gui;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.acgist.snail.Snail;
import com.acgist.snail.context.SystemContext;
import com.acgist.snail.format.BEncodeEncoder;
import com.acgist.snail.gui.event.AlertEvent;
import com.acgist.snail.gui.event.BuildEvent;
import com.acgist.snail.gui.event.ExitEvent;
import com.acgist.snail.gui.event.HideEvent;
import com.acgist.snail.gui.event.NoticeEvent;
import com.acgist.snail.gui.event.RefreshTaskListEvent;
import com.acgist.snail.gui.event.RefreshTaskStatusEvent;
import com.acgist.snail.gui.event.ResponseEvent;
import com.acgist.snail.gui.event.ShowEvent;
import com.acgist.snail.gui.event.TorrentEvent;
import com.acgist.snail.net.application.ApplicationClient;
import com.acgist.snail.pojo.message.ApplicationMessage;
import com.acgist.snail.pojo.message.ApplicationMessage.Type;
import com.acgist.snail.utils.Performance;

public class GuiManagerTest extends Performance {

	/**
	 * <p>测试环境客户端和服务端使用同一套事件</p>
	 * <p>客户端收到事件也会发出通知所以会提示：发送扩展GUI消息失败</p>
	 */
	@BeforeAll
	public static final void registerEvent() {
		GuiManager.register(ShowEvent.getInstance());
		GuiManager.register(HideEvent.getInstance());
		GuiManager.register(ExitEvent.getInstance());
		GuiManager.register(BuildEvent.getInstance());
		GuiManager.register(AlertEvent.getInstance());
		GuiManager.register(NoticeEvent.getInstance());
		GuiManager.register(TorrentEvent.getInstance());
		GuiManager.register(ResponseEvent.getInstance());
		GuiManager.register(RefreshTaskListEvent.getInstance());
		GuiManager.register(RefreshTaskStatusEvent.getInstance());
	}

	@Test
	public void testEvent() {
		final var manager = GuiManager.getInstance();
		manager.alert("acgist", "acgist");
		manager.notice("acgist", "acgist");
		manager.show();
		manager.hide();
		manager.refreshTaskList();
		manager.refreshTaskStatus();
		assertNotNull(manager);
	}
	
	@Test
	public void testExtend() {
		if(SKIP) {
			this.log("跳过扩展GUI测试");
			return;
		}
		LOGGER.info("系统开始启动");
		SystemContext.info();
		SystemContext.build(); // 初始化系统上下文
		if(Snail.available()) {
			final String[] args = new String[] { "mode=extend" }; // 后台模式启动
			GuiManager.getInstance().init(args).build(); // 初始化GUI
		} else {
			LOGGER.debug("启动监听失败");
		}
		LOGGER.info("系统启动完成");
	}
	
	@Test
	public void testSocket() {
		if(SKIP) {
			this.log("跳过扩展GUI测试");
			return;
		}
		String message = null;
		final Scanner scanner = new Scanner(System.in);
		final ApplicationClient client = ApplicationClient.newInstance();
		client.connect();
		// 注册GUI
		client.send(ApplicationMessage.message(Type.GUI, message));
		while ((message = scanner.nextLine()) != null) {
			if(message.equalsIgnoreCase(Type.TEXT.name())) {
				client.send(ApplicationMessage.message(Type.TEXT, message));
			} else if(message.equalsIgnoreCase(Type.CLOSE.name())) {
				client.send(ApplicationMessage.message(Type.CLOSE, message));
				client.close();
				break;
			} else if(message.equalsIgnoreCase(Type.NOTIFY.name())) {
				client.send(ApplicationMessage.message(Type.NOTIFY, message));
			} else if(message.equalsIgnoreCase(Type.SHUTDOWN.name())) {
				client.send(ApplicationMessage.message(Type.SHUTDOWN, message));
				client.close();
				break;
			} else if(message.equalsIgnoreCase(Type.TASK_NEW.name())) {
				final Map<String, String> map = new HashMap<>();
//				map.put("url", "下载地址或者种子文件路径");
				// BT任务
//				map.put("files", "B编码下载文件列表");
				map.put("url", "https://mirror.bit.edu.cn/apache/tomcat/tomcat-9/v9.0.41/bin/apache-tomcat-9.0.41.zip");
				client.send(ApplicationMessage.message(Type.TASK_NEW, BEncodeEncoder.encodeMapString(map)));
			} else if(message.equalsIgnoreCase(Type.TASK_LIST.name())) {
				client.send(ApplicationMessage.message(Type.TASK_LIST, message));
			} else if(message.equalsIgnoreCase(Type.TASK_START.name())) {
				client.send(ApplicationMessage.message(Type.TASK_START, "37f48162-d306-4fff-b161-f1231a3f7e48"));
			} else if(message.equalsIgnoreCase(Type.TASK_PAUSE.name())) {
				client.send(ApplicationMessage.message(Type.TASK_PAUSE, "37f48162-d306-4fff-b161-f1231a3f7e48"));
			} else if(message.equalsIgnoreCase(Type.TASK_DELETE.name())) {
				client.send(ApplicationMessage.message(Type.TASK_DELETE, "37f48162-d306-4fff-b161-f1231a3f7e48"));
			} else {
				client.send(ApplicationMessage.text(message));
			}
		}
		scanner.close();
	}
	
}
