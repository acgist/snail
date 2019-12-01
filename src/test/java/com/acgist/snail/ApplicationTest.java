package com.acgist.snail;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.acgist.snail.net.application.ApplicationClient;
import com.acgist.snail.net.application.ApplicationServer;
import com.acgist.snail.pojo.message.ApplicationMessage;
import com.acgist.snail.pojo.message.ApplicationMessage.Type;
import com.acgist.snail.system.bencode.BEncodeEncoder;
import com.acgist.snail.system.context.SystemThreadContext;

public class ApplicationTest extends BaseTest {

	@Test
	public void testServer() {
		ApplicationServer.getInstance().listen();
		this.pause();
	}
	
	@Test
	public void testClient() {
		final ApplicationClient client = ApplicationClient.newInstance();
		client.connect();
		String message = null;
		Scanner scanner = new Scanner(System.in);
//		while ((message = scanner.next()) != null) { // 使用next()读取时会按照空白行（空格、Tab、Enter）拆分，使用nextLine()不会被拆分。
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
				Map<String, String> map = new HashMap<>();
				map.put("url", "http://mirror.bit.edu.cn/apache/tomcat/tomcat-9/v9.0.24/bin/apache-tomcat-9.0.24-windows-x86.zip");
//				map.put("url", "E:\\snail\\0000.torrent");
//				map.put("files", "l50:[UHA-WINGS][Vinland Saga][01][x264 1080p][CHT].mp4e");
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
	
	@Test
	public void testThread() throws InterruptedException {
		final ApplicationClient client = ApplicationClient.newInstance();
		client.connect();
		var pool = SystemThreadContext.newCacheExecutor("test");
		for (int index = 0; index < 10; index++) {
			pool.submit(() -> {
				while(true) {
					client.send(ApplicationMessage.text("test"));
//					ThreadUtils.sleep(10);
				}
			});
		}
		pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
	}
	
}
