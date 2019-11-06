package com.acgist.snail.net.ws;

import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.IMessageHandler;
import com.acgist.snail.system.exception.NetException;

/**
 * WebSocket消息代理
 * 
 * @author acgist
 * @since 1.1.0
 */
public class WebSocketMessageHandler implements IMessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketMessageHandler.class);
	
	private boolean close = false;
	private final WebSocket socket;
//	private final HttpClient client;
	
	public WebSocketMessageHandler(HttpClient client, WebSocket socket) {
//		this.client = client;
		this.socket = socket;
	}

	@Override
	public boolean available() {
		return !this.close && this.socket != null;
	}

	@Override
	public void send(ByteBuffer buffer, int timeout) throws NetException {
		if(!available()) {
			LOGGER.debug("WebSocket消息发送失败：Socket不可用");
			return;
		}
		if(buffer.position() != 0) {
			buffer.flip();
		}
		if(buffer.limit() == 0) {
			LOGGER.warn("WebSocket消息发送失败：{}", buffer);
			return;
		}
		synchronized (this.socket) {
			try {
				final Future<WebSocket> future = this.socket.sendBinary(buffer, true);
				WebSocket webSocket = null;
				if(timeout <= TIMEOUT_NONE) {
					webSocket = future.get();
				} else {
					webSocket = future.get(timeout, TimeUnit.SECONDS);
				}
				if(webSocket == null) {
					LOGGER.warn("WebSocket消息发送失败：{}", webSocket);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new NetException(e);
			} catch (TimeoutException | ExecutionException e) {
				throw new NetException(e);
			}
		}
	}

	@Override
	public InetSocketAddress remoteSocketAddress() {
		return null;
	}

	@Override
	public void close() {
		this.close = true;
		this.socket.sendClose(WebSocket.NORMAL_CLOSURE, "Close");
		this.socket.abort();
	}

}
