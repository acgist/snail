package com.acgist.snail.module.client.launch;

import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.module.config.SystemConfig;
import com.acgist.snail.module.handler.ConnectHandler;
import com.acgist.snail.module.handler.WriterHandler;
import com.acgist.snail.pojo.message.ClientMessage;
import com.acgist.snail.pojo.message.ClientMessage.Type;
import com.acgist.snail.utils.AioUtils;

/**
 * 窗口唤醒客户端
 */
public class ApplicationNotifyClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationNotifyClient.class);
	
	private AsynchronousChannelGroup group;
	private AsynchronousSocketChannel socket;
	
	/**
	 * 连接
	 */
	public void connect() {
		ExecutorService executor = Executors.newFixedThreadPool(10);
		try {
			group = AsynchronousChannelGroup.withThreadPool(executor);
			socket = AsynchronousSocketChannel.open(group);
			socket.setOption(StandardSocketOptions.TCP_NODELAY, true);
			socket.setOption(StandardSocketOptions.SO_REUSEADDR, true);
			socket.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
		} catch (Exception e) {
			LOGGER.error("客户端连接异常", e);
		}
		socket.connect(new InetSocketAddress(SystemConfig.getServerHost(), SystemConfig.getServerPort()), socket, new ConnectHandler());
	}
	
	/**
	 * 发送消息
	 */
	public void send(ClientMessage message) {
		ByteBuffer buffer = ByteBuffer.wrap(message.toBytes());
		synchronized (socket) { // 防止多线程抛异常
			socket.write(buffer, buffer, new WriterHandler());
		}
	}
	
	/**
	 * 关闭资源
	 */
	public void close() {
		AioUtils.close(socket);
		group.shutdown();
	}
	
	public void readin() {
		Scanner scanner = new Scanner(System.in);
		String message = null;
		while ((message = scanner.next()) != null) {
			if(message.equals("close")) {
				send(ClientMessage.message(Type.close, message));
				close();
				break;
			} else {
				send(ClientMessage.text(message));
			}
		}
		scanner.close();
	}
	
	/**
	 * 唤起主窗口
	 */
	public static final void notifyWindow() {
		ApplicationNotifyClient client = new ApplicationNotifyClient();
		client.connect();
		client.send(ClientMessage.message(ClientMessage.Type.notify));
		client.close();
	}
	
	public static void main(String[] args) {
		ApplicationNotifyClient client = new ApplicationNotifyClient();
		client.connect();
		client.readin();
	}

}
