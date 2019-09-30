package com.acgist.snail.utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.config.SystemConfig;

/**
 * <p>IO工具</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class IoUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(IoUtils.class);
	
	/**
	 * 读取内容
	 */
	public static final String readContent(ByteBuffer buffer) {
		return readContent(buffer, SystemConfig.DEFAULT_CHARSET);
	}
	
	/**
	 * 读取内容
	 */
	public static final String readContent(ByteBuffer buffer, String charset) {
		if(charset == null) {
			charset = SystemConfig.DEFAULT_CHARSET;
		}
		String content = null;
		final CharsetDecoder decoder = Charset.forName(charset).newDecoder();
		decoder.onMalformedInput(CodingErrorAction.IGNORE);
		try {
			if(buffer.position() != 0) {
				buffer.flip();
			}
			content = decoder.decode(buffer).toString();
			buffer.compact();
		} catch (Exception e) {
			LOGGER.error("ByteBuffer解码异常", e);
		}
		return content;
	}
	
	/**
	 * 输入流转为字符串。
	 */
	public static final String ofInputStream(InputStream input, String charset) {
		if(input == null) {
			return null;
		}
		if(charset == null) {
			charset = SystemConfig.DEFAULT_CHARSET;
		}
		int index;
		final char[] chars = new char[1024];
		final StringBuilder builder = new StringBuilder();
		try {
			final var reader = new InputStreamReader(input, charset);
			while((index = reader.read(chars)) != -1) {
				builder.append(new String(chars, 0, index));
			}
		} catch (Exception e) {
			LOGGER.error("读取输入流异常", e);
		}
		return builder.toString();
	}
	
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
