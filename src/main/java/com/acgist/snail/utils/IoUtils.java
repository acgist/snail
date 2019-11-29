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
public final class IoUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(IoUtils.class);
	
	/**
	 * <p>关闭InputStream</p>
	 */
	public static final void close(InputStream input) {
		try {
			if(input != null) {
				input.close();
			}
		} catch (Exception e) {
			LOGGER.error("关闭InputStream异常", e);
		}
	}
	
	/**
	 * <p>关闭OutputStream</p>
	 */
	public static final void close(OutputStream output) {
		try {
			if(output != null) {
				output.flush();
				output.close();
			}
		} catch (Exception e) {
			LOGGER.error("关闭OutputStream异常", e);
		}
	}
	
	/**
	 * <p>关闭Socket</p>
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
	 * <p>关闭AsynchronousSocketChannel</p>
	 */
	public static final void close(AsynchronousSocketChannel socket) {
		if(socket != null && socket.isOpen()) {
			try {
				socket.close();
			} catch (Exception e) {
				LOGGER.error("关闭AsynchronousSocketChannel异常", e);
			}
		}
	}
	
	/**
	 * <p>关闭AsynchronousServerSocketChannel</p>
	 */
	public static final void close(AsynchronousServerSocketChannel server) {
		if(server != null && server.isOpen()) {
			try {
				server.close();
			} catch (Exception e) {
				LOGGER.error("关闭AsynchronousServerSocketChannel异常", e);
			}
		}
	}
	
	/**
	 * <p>关闭AsynchronousChannelGroup</p>
	 */
	public static final void close(AsynchronousChannelGroup group) {
		if(group != null && !group.isShutdown()) {
			group.shutdown();
		}
	}
	
	/**
	 * <p>关闭DatagramChannel</p>
	 */
	public static final void close(DatagramChannel channel) {
		if(channel != null && channel.isOpen()) {
			try {
				channel.close();
			} catch (Exception e) {
				LOGGER.error("关闭DatagramChannel异常", e);
			}
		}
	}

	/**
	 * <p>关闭Selector</p>
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
