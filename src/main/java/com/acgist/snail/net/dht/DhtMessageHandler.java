package com.acgist.snail.net.dht;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.dht.bootstrap.Request;
import com.acgist.snail.net.dht.bootstrap.Response;
import com.acgist.snail.net.dht.bootstrap.request.FindNodeRequest;
import com.acgist.snail.net.dht.bootstrap.request.GetPeersRequest;
import com.acgist.snail.net.dht.bootstrap.request.PingRequest;
import com.acgist.snail.net.dht.bootstrap.response.FindNodeResponse;
import com.acgist.snail.net.dht.bootstrap.response.PingResponse;
import com.acgist.snail.system.bcode.BCodeDecoder;
import com.acgist.snail.system.config.DhtConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.system.manager.NodeManager;
import com.acgist.snail.system.manager.RequestManager;
import com.acgist.snail.utils.ThreadUtils;

/**
 * http://www.bittorrent.org/beps/bep_0005.html
 */
public class DhtMessageHandler extends UdpMessageHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DhtMessageHandler.class);

	private static final Duration TIMEOUT = Duration.ofSeconds(4);
	
	private static final Function<Request, Response> RESPONSE = (request) -> {
		if(request == null) {
			return null;
		}
		final Response response = request.getResponse();
		if(response == null) {
			LOGGER.warn("响应超时");
		} else if(!response.success()) {
			LOGGER.warn("DHT响应返回失败：{}-{}", response.errorCode(), response.errorMessage());
		}
		return response;
	};
	
	public static final Function<Response, Boolean> SUCCESS = (response) -> {
		return response != null && response.success();
	};
	
	@Override
	public void onMessage(InetSocketAddress address, ByteBuffer buffer) {
		LOGGER.debug("DHT消息，地址：{}", address);
		buffer.flip();
		final byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		final BCodeDecoder decoder = BCodeDecoder.newInstance(bytes);
		decoder.mustMap();
		final String y = decoder.getString(DhtConfig.KEY_Y);
		LOGGER.debug("DHT消息类型：{}", y);
		if(DhtConfig.KEY_Q.equals(y)) {
			final Request request = Request.valueOf(decoder);
			onRequest(address, request);
		} else if(DhtConfig.KEY_R.equals(y)) {
			final Response response = Response.valueOf(decoder);
			onResponse(response);
		} else {
			LOGGER.warn("不支持的DHT类型：{}", y);
		}
	}
	
	private void onRequest(final SocketAddress address, final Request request) {
		Response response = null;
		switch (request.getQ()) {
		case ping:
			response = ping(request);
			break;
		case find_node:
			response = findNode(request);
			break;
		case get_peers:
			response = getPeers(request);
			break;
		case announce_peer:
			response = announcePeer(request);
			break;
		}
		pushMessage(response, address);
	}
	
	private void onResponse(final Response response) {
		final Request request = RequestManager.getInstance().response(response);
		if(request == null) {
			LOGGER.warn("未找到响应对应的请求");
			return;
		}
		switch (request.getQ()) {
		case ping:
			ping(request, response);
			break;
		case find_node:
			findNode(request, response);
			break;
		case get_peers:
			findNode(request, response);
			break;
		case announce_peer:
			findNode(request, response);
			break;
		}
	}
	
	/**
	 * 检测节点是否可达：阻塞
	 */
	public boolean ping(SocketAddress address) {
		final PingRequest request = PingRequest.newRequest();
		pushMessage(request, address);
		waitResponse(request);
		take(request);
		final Response response = RESPONSE.apply(request);
		if(SUCCESS.apply(response)) {
			return true;
		}
		return false;
	}

	private Response ping(Request request) {
		return PingResponse.newInstance(request);
	}
	
	private void ping(Request request, Response response) {
		notifyRequest(request);
	}
	
	/**
	 * 查找给定ID的DHT节点的联系信息，回复被请求节点的路由表中距离请求target最接近的K个node的ID和INFO
	 */
	public void findNode(SocketAddress address, byte[] target) {
		final FindNodeRequest request = FindNodeRequest.newRequest(target);
		pushMessage(request, address);
	}
	
	private Response findNode(Request request) {
		return FindNodeResponse.newInstance(request);
	}
	
	private void findNode(Request request, Response response) {
		take(request);
		if(SUCCESS.apply(response)) {
			final var list = FindNodeResponse.newInstance(response).getNodes();
			NodeManager.getInstance().put(list);
		}
	}

	/**
	 * 获取Peer
	 */
	public void getPeers(SocketAddress address, byte[] infoHash) {
		final GetPeersRequest request = GetPeersRequest.newRequest(infoHash);
		pushMessage(request, address);
	}

	private Response getPeers(Request request) {
		return null;
	}
	
	private void getPeers(Request request, Response response) {
//		waitResponse(request);
//		take(request);
//		final Response response = RESPONSE.apply(request);
//		if(SUCCESS.apply(response)) {
//			return FindNodeResponse.newInstance(response).getNodes();
//		}
//		return null;
	}
	
	/**
	 * 表明发出announce_peer请求的节点，正在某个端口下载torrent文件
	 */
	public void announcePeer() {
	}
	
	public Response announcePeer(Request request) {
		return null;
	}
	
	public void announcePeer(Request request, Response response) {
	}
	
	/**
	 * 取走响应
	 */
	private Request take(Request request) {
		return RequestManager.getInstance().take(request);
	}

	/**
	 * 等待响应
	 */
	private void waitResponse(Request request) {
		synchronized (request) {
			if(!request.response()) {
				ThreadUtils.wait(request, TIMEOUT);
			}
		}
	}
	
	/**
	 * 唤醒等待
	 */
	private void notifyRequest(Request request) {
		synchronized (request) {
			request.notifyAll();
		}
	}
	
	private void pushMessage(Request request, SocketAddress address) {
		RequestManager.getInstance().put(request);
		final ByteBuffer buffer = ByteBuffer.wrap(request.toBytes());
		pushMessage(buffer, address);
	}
	
	private void pushMessage(Response response, SocketAddress address) {
		final ByteBuffer buffer = ByteBuffer.wrap(response.toBytes());
		pushMessage(buffer, address);
	}
	
	private void pushMessage(ByteBuffer buffer, SocketAddress address) {
		try {
			this.send(buffer, address);
		} catch (NetException e) {
			LOGGER.error("发送UDP消息异常", e);
		}
	}

}
