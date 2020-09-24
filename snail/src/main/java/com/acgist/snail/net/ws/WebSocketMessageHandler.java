package com.acgist.snail.net.ws;

import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.net.IMessageHandler;

/**
 * <p>WebSocket消息代理</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public abstract class WebSocketMessageHandler implements IMessageHandler, WebSocket.Listener {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketMessageHandler.class);
	
	/**
	 * <p>是否关闭</p>
	 */
	private boolean close = false;
	/**
	 * <p>WebSocket</p>
	 */
	protected WebSocket socket;
	/**
	 * <p>HttpClient</p>
	 */
	protected HttpClient client;
	
	/**
	 * <p>代理Socket、Client</p>
	 * 
	 * @param socket socket
	 * @param client client
	 */
	public void handle(WebSocket socket, HttpClient client) {
		this.socket = socket;
		this.client = client;
	}
	
	@Override
	public boolean available() {
		return !this.close && this.socket != null;
	}

	@Override
	public void send(ByteBuffer buffer, int timeout) throws NetException {
		if(!this.available()) {
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
	
	@Override
	public void onOpen(WebSocket webSocket) {
		LOGGER.debug("WebSocket连接成功：{}", webSocket);
		WebSocket.Listener.super.onOpen(webSocket);
	}
	
	@Override
	public CompletionStage<?> onText(WebSocket webSocket, CharSequence message, boolean last) {
		LOGGER.debug("WebSocket接收数据（Text）：{}", message);
		return WebSocket.Listener.super.onText(webSocket, message, last);
	}
	
	@Override
	public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer message, boolean last) {
		LOGGER.debug("WebSocket接收数据（Binary）：{}", message);
		return WebSocket.Listener.super.onBinary(webSocket, message, last);
	}
	
	@Override
	public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
		LOGGER.debug("WebSocket Ping");
		return WebSocket.Listener.super.onPing(webSocket, message);
	}
	
	@Override
	public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
		LOGGER.debug("WebSocket Pong");
		return WebSocket.Listener.super.onPong(webSocket, message);
	}
	
	@Override
	public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
		LOGGER.debug("WebSocket关闭：{}-{}", statusCode, reason);
		return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
	}
	
	@Override
	public void onError(WebSocket webSocket, Throwable error) {
		LOGGER.error("WebSocket异常", error);
		WebSocket.Listener.super.onError(webSocket, error);
	}

}
