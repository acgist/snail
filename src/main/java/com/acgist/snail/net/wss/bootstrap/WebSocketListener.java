package com.acgist.snail.net.wss.bootstrap;

import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebSocket监听器
 * 
 * @author acgist
 * @since 1.1.0
 */
public class WebSocketListener implements WebSocket.Listener {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketListener.class);
	
	@Override
	public void onOpen(WebSocket webSocket) {
		LOGGER.debug("WebSocket打开成功：{}", webSocket);
		WebSocket.Listener.super.onOpen(webSocket);
	}
	
	@Override
	public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
		LOGGER.debug("WebSocket接收数据：{}", data);
		return WebSocket.Listener.super.onText(webSocket, data, last);
	}
	
	@Override
	public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
		LOGGER.debug("WebSocket接收数据：{}", data);
		return WebSocket.Listener.super.onBinary(webSocket, data, last);
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
		LOGGER.debug("WebSocket关闭：{}，{}", statusCode, reason);
		return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
	}
	
	@Override
	public void onError(WebSocket webSocket, Throwable error) {
		LOGGER.debug("WebSocket异常", error);
		WebSocket.Listener.super.onError(webSocket, error);
	}
	
}
