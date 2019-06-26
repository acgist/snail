package com.acgist.snail.net.wss.bootstrap;

import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletionStage;

/**
 * WebSocket监听器
 * 
 * @author acgist
 * @since 1.1.0
 */
public class WebSocketListener implements WebSocket.Listener {

	@Override
	public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
		System.out.println("----");
		return WebSocket.Listener.super.onBinary(webSocket, data, last);
	}
	
	@Override
	public void onError(WebSocket webSocket, Throwable error) {
		WebSocket.Listener.super.onError(webSocket, error);
	}
	
}
