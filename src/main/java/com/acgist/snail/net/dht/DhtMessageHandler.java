package com.acgist.snail.net.dht;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.dht.bootstrap.Request;
import com.acgist.snail.net.dht.bootstrap.Response;
import com.acgist.snail.net.dht.bootstrap.request.AnnouncePeerRequest;
import com.acgist.snail.net.dht.bootstrap.request.FindNodeRequest;
import com.acgist.snail.net.dht.bootstrap.request.GetPeersRequest;
import com.acgist.snail.net.dht.bootstrap.request.PingRequest;
import com.acgist.snail.net.dht.bootstrap.response.FindNodeResponse;
import com.acgist.snail.net.dht.bootstrap.response.GetPeersResponse;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.system.bcode.BCodeDecoder;
import com.acgist.snail.system.config.DhtConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.system.manager.NodeManager;
import com.acgist.snail.system.manager.RequestManager;
import com.acgist.snail.utils.ThreadUtils;

/**
 * <p>DHT消息</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0005.html</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class DhtMessageHandler extends UdpMessageHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DhtMessageHandler.class);

	private static final Duration TIMEOUT = Duration.ofSeconds(4);
	
	/**
	 * 获取响应
	 */
	private static final Function<Request, Response> RESPONSE = (request) -> {
		if(request == null) {
			return null;
		}
		final Response response = request.getResponse();
		if(response == null) {
			LOGGER.warn("DHT响应超时");
		} else if(!response.success()) {
			LOGGER.warn("DHT响应返回失败：{}-{}", response.errorCode(), response.errorMessage());
		}
		return response;
	};
	
	/**
	 * 判断响应是否成功
	 */
	public static final Function<Response, Boolean> SUCCESS = (response) -> {
		return response != null && response.success();
	};
	
	@Override
	public void onMessage(ByteBuffer buffer, InetSocketAddress address) {
		LOGGER.debug("DHT消息，地址：{}", address);
		buffer.flip();
		final byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		final BCodeDecoder decoder = BCodeDecoder.newInstance(bytes);
		final var map = decoder.nextMap();
		if(map == null) {
			LOGGER.warn("DHT消息格式错误：{}", decoder.obbString());
			return;
		}
		final String y = decoder.getString(DhtConfig.KEY_Y);
		LOGGER.debug("DHT消息类型：{}", y);
		if(DhtConfig.KEY_Q.equals(y)) {
			final Request request = Request.valueOf(decoder);
			request.setAddress(address);
			onRequest(request, address);
		} else if(DhtConfig.KEY_R.equals(y)) {
			final Response response = Response.valueOf(decoder);
			response.setAddress(address);
			onResponse(response);
		} else {
			LOGGER.warn("不支持的DHT类型：{}", y);
		}
	}
	
	/**
	 * 处理请求
	 * 
	 * @param request 请求
	 * @param address 客户端地址
	 */
	private void onRequest(final Request request, final InetSocketAddress address) {
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
	
	/**
	 * <p>处理响应</p>
	 * <p>处理响应，同时删除请求列表中的请求。</p>
	 * 
	 * @param response 响应
	 */
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
			getPeers(request, response);
			break;
		case announce_peer:
			announcePeer(request, response);
			break;
		}
	}
	
	/**
	 * <p>发送请求：Ping</p>
	 * <p>检测节点是否可达，该方法同步阻塞。响应后添加列表</p>
	 */
	public NodeSession ping(InetSocketAddress address) {
		final PingRequest request = PingRequest.newRequest();
		pushMessage(request, address);
		waitResponse(request);
		final Response response = RESPONSE.apply(request);
		if(SUCCESS.apply(response)) {
			final NodeSession nodeSession = NodeManager.getInstance().newNodeSession(response.getNodeId(), address.getHostString(), address.getPort());
			NodeManager.getInstance().sortNodes();
			return nodeSession;
		}
		return null;
	}

	/**
	 * 处理请求：Ping
	 */
	private Response ping(Request request) {
		return PingRequest.execute(request);
	}

	/**
	 * 处理响应：Ping，唤醒Ping等待。
	 */
	private void ping(Request request, Response response) {
		notifyRequest(request);
	}
	
	/**
	 * 发送请求：findNode
	 */
	public void findNode(InetSocketAddress address, byte[] target) {
		final FindNodeRequest request = FindNodeRequest.newRequest(target);
		pushMessage(request, address);
	}
	
	/**
	 * 处理请求：findNode
	 */
	private Response findNode(Request request) {
		return FindNodeRequest.execute(request);
	}
	
	/**
	 * 处理响应：findNode
	 */
	private void findNode(Request request, Response response) {
		if(SUCCESS.apply(response)) {
			FindNodeResponse.newInstance(response).getNodes();
		}
	}

	/**
	 * 发送请求：getPeers
	 */
	public void getPeers(InetSocketAddress address, byte[] infoHash) {
		final GetPeersRequest request = GetPeersRequest.newRequest(infoHash);
		pushMessage(request, address);
	}

	/**
	 * 处理请求：getPeers
	 */
	private Response getPeers(Request request) {
		return GetPeersRequest.execute(request);
	}

	/**
	 * 处理响应：getPeers，同时设置Node Token。
	 */
	private void getPeers(Request request, Response response) {
		if(SUCCESS.apply(response)) {
			final GetPeersResponse getPeersResponse = GetPeersResponse.newInstance(response);
			if(getPeersResponse.havePeers()) {
				getPeersResponse.getPeers(request);
			}
			if(getPeersResponse.haveNodes()) {
				getPeersResponse.getNodes();
			}
			final byte[] token = getPeersResponse.getToken();
			final byte[] nodeId = getPeersResponse.getNodeId();
			NodeManager.getInstance().token(nodeId, request, token);
		}
	}
	
	/**
	 * 发送请求：announcePeer
	 */
	public void announcePeer(InetSocketAddress address, byte[] token, byte[] infoHash) {
		final AnnouncePeerRequest request = AnnouncePeerRequest.newRequest(token, infoHash);
		pushMessage(request, address);
	}
	
	/**
	 * 处理请求：announcePeer
	 */
	private Response announcePeer(Request request) {
		return AnnouncePeerRequest.execute(request);
	}

	/**
	 * 处理响应：announcePeer
	 */
	private void announcePeer(Request request, Response response) {
		if(SUCCESS.apply(response)) {
		}
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
	 * 唤醒响应等待
	 */
	private void notifyRequest(Request request) {
		synchronized (request) {
			request.notifyAll();
		}
	}
	
	/**
	 * 发送请求
	 */
	private void pushMessage(Request request, InetSocketAddress address) {
		request.setAddress(address);
		RequestManager.getInstance().put(request);
		final ByteBuffer buffer = ByteBuffer.wrap(request.toBytes());
		pushMessage(buffer, address);
	}
	
	/**
	 * 发送响应
	 */
	private void pushMessage(Response response, InetSocketAddress address) {
		final ByteBuffer buffer = ByteBuffer.wrap(response.toBytes());
		pushMessage(buffer, address);
	}
	
	/**
	 * 发送数据
	 */
	private void pushMessage(ByteBuffer buffer, InetSocketAddress address) {
		try {
			this.send(buffer, address);
		} catch (NetException e) {
			LOGGER.error("发送UDP消息异常", e);
		}
	}

}
