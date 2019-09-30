package com.acgist.snail.net.wss;

import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

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
//	private final HttpClient client;
	private final WebSocket socket;
	/**
	 * 写入锁，每次只允许一个写入。
	 */
	private final Semaphore writeableLock = new Semaphore(1);
	
	public WebSocketMessageHandler(HttpClient client, WebSocket socket) {
//		this.client = client;
		this.socket = socket;
	}

	@Override
	public boolean available() {
		return !this.close && this.socket != null;
	}

	@Override
	public void send(ByteBuffer buffer) throws NetException {
		if(!available()) {
			LOGGER.debug("发送消息时Socket已经不可用");
			return;
		}
		if(buffer.position() != 0) {
			buffer.flip();
		}
		if(buffer.limit() == 0) {
			LOGGER.warn("发送消息为空");
			return;
		}
		try {
			this.writeableLock.acquire();
			final Future<WebSocket> future = this.socket.sendBinary(buffer, true);
			final WebSocket webSocket = future.get(SEND_TIMEOUT, TimeUnit.SECONDS);
			if(webSocket == null) {
				LOGGER.warn("发送数据为空");
			}
		} catch (Exception e) {
			throw new NetException(e);
		} finally {
			this.writeableLock.release();
		}
	}

	@Override
	public InetSocketAddress remoteSocketAddress() {
		return null;
	}

	@Override
	public void close() {
		this.close = true;
		synchronized (this.socket) {
			this.socket.sendClose(WebSocket.NORMAL_CLOSURE, "Close");
			this.socket.abort();
		}
	}

}
