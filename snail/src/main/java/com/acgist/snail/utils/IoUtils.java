package com.acgist.snail.utils;

import java.io.Closeable;
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
	 * <p>工具类禁止实例化</p>
	 */
	private IoUtils() {
	}
	
	/**
	 * <p>关闭{@code Closeable}</p>
	 * 
	 * @param closeable {@code Closeable}
	 */
	public static final void close(Closeable closeable) {
		try {
			if(closeable != null) {
				closeable.close();
			}
		} catch (Exception e) {
			LOGGER.error("关闭Closeable异常", e);
		}
	}
	
	/**
	 * <p>关闭{@code InputStream}</p>
	 * 
	 * @param input {@code InputStream}
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
	 * <p>关闭{@code OutputStream}</p>
	 * 
	 * @param output {@code OutputStream}
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
	 * <p>关闭{@code Socket}</p>
	 * 
	 * @param socket {@code Socket}
	 */
	public static final void close(Socket socket) {
		try {
			if(socket != null && !socket.isClosed()) {
				socket.close();
			}
		} catch (Exception e) {
			LOGGER.error("关闭Socket异常", e);
		}
	}
	
	/**
	 * <p>关闭{@code AsynchronousSocketChannel}</p>
	 * 
	 * @param socket {@code AsynchronousSocketChannel}
	 */
	public static final void close(AsynchronousSocketChannel socket) {
		try {
			if(socket != null && socket.isOpen()) {
				socket.close();
			}
		} catch (Exception e) {
			LOGGER.error("关闭AsynchronousSocketChannel异常", e);
		}
	}
	
	/**
	 * <p>关闭{@code AsynchronousServerSocketChannel}</p>
	 * 
	 * @param server {@code AsynchronousServerSocketChannel}
	 */
	public static final void close(AsynchronousServerSocketChannel server) {
		try {
			if(server != null && server.isOpen()) {
				server.close();
			}
		} catch (Exception e) {
			LOGGER.error("关闭AsynchronousServerSocketChannel异常", e);
		}
	}
	
	/**
	 * <p>关闭{@code AsynchronousChannelGroup}</p>
	 * 
	 * @param group {@code AsynchronousChannelGroup}
	 */
	public static final void close(AsynchronousChannelGroup group) {
		try {
			if(group != null && !group.isShutdown()) {
				group.shutdown();
			}
		} catch (Exception e) {
			LOGGER.error("关闭AsynchronousChannelGroup异常", e);
		}
	}
	
	/**
	 * <p>关闭{@code DatagramChannel}</p>
	 * 
	 * @param channel {@code DatagramChannel}
	 */
	public static final void close(DatagramChannel channel) {
		try {
			if(channel != null && channel.isOpen()) {
				channel.close();
			}
		} catch (Exception e) {
			LOGGER.error("关闭DatagramChannel异常", e);
		}
	}

	/**
	 * <p>关闭{@code Selector}</p>
	 * 
	 * @param selector {@code Selector}
	 */
	public static final void close(Selector selector) {
		try {
			if(selector != null && selector.isOpen()) {
				selector.close();
			}
		} catch (Exception e) {
			LOGGER.error("关闭Selector异常", e);
		}
	}

}
