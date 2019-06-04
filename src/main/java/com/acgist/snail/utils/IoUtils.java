package com.acgist.snail.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.DatagramChannel;
import java.nio.charset.CharacterCodingException;
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
	public static final String readContent(ByteBuffer attachment) {
		final CharsetDecoder decoder = Charset.forName(SystemConfig.DEFAULT_CHARSET).newDecoder();
		decoder.onMalformedInput(CodingErrorAction.IGNORE);
		String content = null;
		try {
			attachment.flip();
			content = decoder.decode(attachment).toString();
			attachment.compact();
		} catch (CharacterCodingException e) {
			LOGGER.error("ByteBuffer解码异常", e);
		}
		return content;
	}
	
	/**
	 * 关闭输入流
	 */
	public static final void close(InputStream input) {
		try {
			if(input != null) {
				input.close();
			}
		} catch (IOException e) {
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
		} catch (IOException e) {
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
			} catch (IOException e) {
				LOGGER.error("关闭Socket输入流异常", e);
			}
			try {
				if(!socket.isOutputShutdown()) {
					socket.shutdownOutput();
				}
			} catch (IOException e) {
				LOGGER.error("关闭Socket输出流异常", e);
			}
			try {
				socket.close(); // 不用判断是否close
			} catch (IOException e) {
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
			} catch (IOException e) {
				LOGGER.error("关闭Socket输出流异常", e);
			}
			try {
				socket.close();
			} catch (IOException e) {
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
			} catch (IOException e) {
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
			} catch (IOException e) {
				LOGGER.error("关闭UDP Channel异常", e);
			}
		}
	}

}
