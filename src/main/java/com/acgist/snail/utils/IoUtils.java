package com.acgist.snail.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Selector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>IO工具</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class IoUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(IoUtils.class);
	
	/**
	 * 关闭输入流
	 */
	public static final void close(InputStream input) {
		try {
			if(input != null) {
				input.close();
			}
		} catch (Exception e) {
			LOGGER.error("关闭输入流异常", e);
		}
	}
	
	/**
	 * 关闭输出流
	 */
	public static final void close(OutputStream output) {
		try {
			if(output != null) {
				output.flush();
				output.close();
			}
		} catch (Exception e) {
			LOGGER.error("关闭输出流异常", e);
		}
	}
	
	/**
	 * 关闭Socket
	 */
	public static final void close(Socket socket) {
		if(socket != null && !socket.isClosed()) {
			try {
				socket.close();
			} catch (Exception e) {
				LOGGER.error("关闭Socket异常", e);
			}
		}
	}
	
	/**
	 * 关闭异步Socket
	 */
	public static final void close(AsynchronousSocketChannel socket) {
		if(socket != null && socket.isOpen()) {
			try {
				socket.close();
			} catch (Exception e) {
				LOGGER.error("关闭异步Socket异常", e);
			}
		}
	}
	
	/**
	 * 关闭异步Socket Server
	 */
	public static final void close(AsynchronousServerSocketChannel server) {
		if(server != null && server.isOpen()) {
			try {
				server.close();
			} catch (Exception e) {
				LOGGER.error("关闭异步Socket Server异常", e);
			}
		}
	}
	
	/**
	 * 关闭异步通道组
	 */
	public static final void close(AsynchronousChannelGroup group) {
		if(group != null && !group.isShutdown()) {
			group.shutdown();
		}
	}
	
	/**
	 * 关闭UDP Channel
	 */
	public static final void close(DatagramChannel channel) {
		if(channel != null && channel.isOpen()) {
			try {
				channel.close();
			} catch (Exception e) {
				LOGGER.error("关闭UDP Channel异常", e);
			}
		}
	}

	/**
	 * 关闭Selector
	 */
	public static final void close(Selector selector) {
		if(selector != null && selector.isOpen()) {
			try {
				selector.close();
			} catch (Exception e) {
				LOGGER.error("关闭Selector异常", e);
			}
		}
	}

}
