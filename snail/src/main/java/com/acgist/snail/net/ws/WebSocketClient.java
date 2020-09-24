package com.acgist.snail.net.ws;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.acgist.snail.exception.NetException;
import com.acgist.snail.net.ClientMessageHandlerAdapter;
import com.acgist.snail.net.IMessageHandler;
import com.acgist.snail.net.http.HTTPClient;

/**
 * <p>WebSocket客户端</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public abstract class WebSocketClient<T extends WebSocketMessageHandler> extends ClientMessageHandlerAdapter<T> implements IMessageHandler {

	/**
	 * <p>WebSocket客户端</p>
	 * 
	 * @param url 地址
	 * @param connectTimeout 超时时间（连接）
	 * @param receiveTimeout 超时时间（接收）
	 * @param handler WebSocket消息代理
	 * 
	 * @throws NetException 网络异常
	 */
	protected WebSocketClient(String url, int connectTimeout, int receiveTimeout, T handler) throws NetException {
		super(handler);
		this.buildWebSocket(url, connectTimeout, receiveTimeout);
	}
	
	/**
	 * <p>WebSocket客户端</p>
	 * 
	 * @param url 地址
	 * @param connectTimeout 超时时间（连接）
	 * @param receiveTimeout 超时时间（接收）
	 * 
	 * @throws NetException 网络异常
	 */
	private void buildWebSocket(String url, int connectTimeout, int receiveTimeout) throws NetException {
		final HttpClient client = HTTPClient.newClient(connectTimeout);
		final CompletableFuture<WebSocket> future = client
			.newWebSocketBuilder()
			.connectTimeout(Duration.ofSeconds(connectTimeout))
			.buildAsync(URI.create(url), this.handler);
		try {
			final WebSocket socket = future.get(receiveTimeout, TimeUnit.SECONDS);
			this.handler.handle(socket, client);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new NetException("WebSocket创建失败", e);
		} catch (TimeoutException | ExecutionException e) {
			throw new NetException("WebSocket创建失败", e);
		}
	}

}
