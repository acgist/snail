package com.acgist.main;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.acgist.gui.extend.ExtendGuiManager;
import com.acgist.snail.format.BEncodeEncoder;
import com.acgist.snail.net.application.ApplicationClient;
import com.acgist.snail.pojo.message.ApplicationMessage;
import com.acgist.snail.pojo.message.ApplicationMessage.Type;

/**
 * <p>Snail启动类</p>
 * <p>通过系统消息管理Snail（蜗牛）</p>
 * <p>后台模式启动：`snail-javafx`</p>
 * <p>扩展GUI可以自行实现Socket连接和消息处理</p>
 * 
 * @author acgist
 * @since 1.4.0
 */
public final class SocketApplication {

	/**
	 * <p>启动方法</p>
	 * 
	 * @param args 启动参数
	 */
	public static final void main(String[] args) {
		ExtendGuiManager.getInstance().registerEvent(); // 注册事件
		createSocketMessage();
	}

	/**
	 * <p>连接Socket使用系统消息</p>
	 * <p>优先发送GUI命令注册扩展GUI，然后发送其他命令进行操作。</p>
	 */
	private static void createSocketMessage() {
		String message = null;
		final Scanner scanner = new Scanner(System.in);
		final ApplicationClient client = ApplicationClient.newInstance();
		client.connect();
		while ((message = scanner.nextLine()) != null) {
			if(message.equalsIgnoreCase(Type.CLOSE.name())) {
				client.send(ApplicationMessage.message(Type.CLOSE, message));
				client.close();
				break;
			} else if(message.equalsIgnoreCase(Type.SHUTDOWN.name())) {
				client.send(ApplicationMessage.message(Type.SHUTDOWN, message));
				client.close();
				break;
			} else if(message.equalsIgnoreCase(Type.GUI.name())) {
				client.send(ApplicationMessage.message(Type.GUI, message));
			} else if(message.equalsIgnoreCase(Type.NOTIFY.name())) {
				client.send(ApplicationMessage.message(Type.NOTIFY, message));
			} else if(message.equalsIgnoreCase(Type.TASK_NEW.name())) {
				final Map<String, String> map = new HashMap<>();
				// 单个文件任务
				map.put("url", "下载地址");
				// BT任务
//				map.put("url", "种子文件路径");
//				map.put("files", "B编码下载文件列表");
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