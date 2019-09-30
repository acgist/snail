package com.acgist.snail.net.wss;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.acgist.snail.net.ClientMessageHandlerAdapter;
import com.acgist.snail.net.IMessageHandler;
import com.acgist.snail.net.http.HTTPClient;
import com.acgist.snail.net.wss.bootstrap.WebSocketListener;
import com.acgist.snail.system.exception.NetException;

/**
 * WebSocket客户端
 * 
 * @author acgist
 * @since 1.1.0
 */
public class WebSocketClient extends ClientMessageHandlerAdapter<WebSocketMessageHandler> implements IMessageHandler {

	private WebSocketClient(HttpClient client, WebSocket socket) {
		super(new WebSocketMessageHandler(client, socket));
	}
	
	public static final WebSocketClient newInstance(String url) throws NetException {
		return newInstance(url, CONNECT_TIMEOUT);
	}
	
	public static final WebSocketClient newInstance(String url, int timeout) throws NetException {
		final HttpClient client = HTTPClient.newClient(timeout);
		final CompletableFuture<WebSocket> future = newWebSocket(client, url, timeout);
		try {
			return new WebSocketClient(client, future.get(timeout, TimeUnit.SECONDS));
		} catch (Exception e) {
			throw new NetException("创建WebSocket失败", e);
		}
	}
	
	private static final CompletableFuture<WebSocket> newWebSocket(HttpClient client, String url, int timeout) {
		return client
			.newWebSocketBuilder()
			.connectTimeout(Duration.ofSeconds(timeout))
			.buildAsync(URI.create(url), new WebSocketListener());
	}

}
