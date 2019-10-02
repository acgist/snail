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
				if(!socket.isInputShutdown()) {
					socket.shutdownInput();
				}
			} catch (Exception e) {
				LOGGER.error("关闭Socket输入流异常", e);
			}
			try {
				if(!socket.isOutputShutdown()) {
					socket.shutdownOutput();
				}
			} catch (Exception e) {
				LOGGER.error("关闭Socket输出流异常", e);
			}
			try {
				socket.close(); // 不用判断是否close
			} catch (Exception e) {
				LOGGER.error("关闭Socket异常", e);
			}
		}
	}
	
	/**
	 * 关闭异步Socket通道
	 */
	public static final void close(AsynchronousSocketChannel socket) {
		if(socket != null && socket.isOpen()) {
			try {
				socket.shutdownInput();
			} catch (Exception e) {
				LOGGER.error("关闭Socket输入流异常", e);
			}
			try {
				socket.shutdownOutput();
			} catch (Exception e) {
				LOGGER.error("关闭Socket输出流异常", e);
			}
			try {
				socket.close();
			} catch (Exception e) {
				LOGGER.error("关闭Socket异常", e);
			}
		}
	}
	
	/**
	 * 关闭异步SocketServer通道
	 */
	public static final void close(AsynchronousServerSocketChannel server) {
		if(server != null && server.isOpen()) {
			try {
				server.close();
			} catch (Exception e) {
				LOGGER.error("关闭Socket Server异常", e);
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
	 * 关闭UDP通道
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
	public static void close(Selector selector) {
		if(selector != null && selector.isOpen()) {
			try {
				selector.close();
			} catch (Exception e) {
				LOGGER.error("关闭Selector异常", e);
			}
		}
	}

}
