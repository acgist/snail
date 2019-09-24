package com.acgist.snail.net.torrent.dht;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.dht.bootstrap.NodeManager;
import com.acgist.snail.net.torrent.dht.bootstrap.Request;
import com.acgist.snail.net.torrent.dht.bootstrap.RequestManager;
import com.acgist.snail.net.torrent.dht.bootstrap.Response;
import com.acgist.snail.net.torrent.dht.bootstrap.request.AnnouncePeerRequest;
import com.acgist.snail.net.torrent.dht.bootstrap.request.FindNodeRequest;
import com.acgist.snail.net.torrent.dht.bootstrap.request.GetPeersRequest;
import com.acgist.snail.net.torrent.dht.bootstrap.request.PingRequest;
import com.acgist.snail.net.torrent.dht.bootstrap.response.FindNodeResponse;
import com.acgist.snail.net.torrent.dht.bootstrap.response.GetPeersResponse;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.bencode.BEncodeDecoder;
import com.acgist.snail.system.config.DhtConfig;
import com.acgist.snail.system.config.DhtConfig.ErrorCode;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.ThreadUtils;

/**
 * <p>DHT消息</p>
 * <p>DHT Protocol</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0005.html</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class DhtMessageHandler extends UdpMessageHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DhtMessageHandler.class);

	/**
	 * 获取响应
	 */
	private static final Function<Request, Response> RESPONSE_GETTER = (request) -> {
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
	public static final Function<Response, Boolean> SUCCESS_VERIFY = (response) -> {
		return response != null && response.success();
	};
	
	@Override
	public void onReceive(ByteBuffer buffer, InetSocketAddress socketAddress) {
		buffer.flip();
		final byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		final BEncodeDecoder decoder = BEncodeDecoder.newInstance(bytes);
		final var map = decoder.nextMap();
		if(map == null) {
			LOGGER.warn("DHT消息格式错误：{}", decoder.oddString());
			return;
		}
		final String y = decoder.getString(DhtConfig.KEY_Y);
		if(DhtConfig.KEY_Q.equals(y)) {
			final Request request = Request.valueOf(decoder);
			request.setSocketAddress(socketAddress);
			onRequest(request, socketAddress);
		} else if(DhtConfig.KEY_R.equals(y)) {
			final Response response = Response.valueOf(decoder);
			response.setSocketAddress(socketAddress);
			onResponse(response);
		} else {
			LOGGER.warn("不支持的DHT类型：{}", y);
		}
	}
	
	/**
	 * 处理请求
	 * 
	 * @param request 请求
	 * @param socketAddress 客户端地址
	 */
	private void onRequest(final Request request, final InetSocketAddress socketAddress) {
		Response response = null;
		LOGGER.debug("DHT收到请求：{}", request.getQ());
		if(request.getQ() == null) {
			final Response error = Response.error(request.getT(), ErrorCode.E_204.code(), "不支持的请求类型");
			pushMessage(error, socketAddress);
			return;
		}
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
		pushMessage(response, socketAddress);
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
		LOGGER.debug("DHT收到响应：{}", request.getQ());
		if(request.getQ() == null) {
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
	public NodeSession ping(InetSocketAddress socketAddress) {
		final PingRequest request = PingRequest.newRequest();
		pushMessage(request, socketAddress);
		waitResponse(request);
		final Response response = RESPONSE_GETTER.apply(request);
		if(SUCCESS_VERIFY.apply(response)) {
			final NodeSession nodeSession = NodeManager.getInstance().newNodeSession(response.getNodeId(), socketAddress.getHostString(), socketAddress.getPort());
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
	public void findNode(InetSocketAddress socketAddress, byte[] target) {
		final FindNodeRequest request = FindNodeRequest.newRequest(target);
		pushMessage(request, socketAddress);
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
		if(SUCCESS_VERIFY.apply(response)) {
			FindNodeResponse.newInstance(response).getNodes();
		}
	}

	/**
	 * 发送请求：getPeers
	 */
	public void getPeers(InetSocketAddress socketAddress, byte[] infoHash) {
		final GetPeersRequest request = GetPeersRequest.newRequest(infoHash);
		pushMessage(request, socketAddress);
	}

	/**
	 * 处理请求：getPeers
	 */
	private Response getPeers(Request request) {
		final byte[] infoHash = request.getBytes(DhtConfig.KEY_INFO_HASH);
		final String infoHashHex = StringUtils.hex(infoHash);
		final TorrentSession torrentSession = TorrentManager.getInstance().torrentSession(infoHashHex);
		if(torrentSession != null) {
			SystemThreadContext.submit(() -> {
				final byte[] token = request.getBytes(DhtConfig.KEY_TOKEN);
				this.announcePeer(request.getSocketAddress(), token, infoHash);
			});
		}
		return GetPeersRequest.execute(request);
	}

	/**
	 * 处理响应：getPeers，同时设置Node Token。
	 */
	private void getPeers(Request request, Response response) {
		if(SUCCESS_VERIFY.apply(response)) {
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
	public void announcePeer(InetSocketAddress socketAddress, byte[] token, byte[] infoHash) {
		final AnnouncePeerRequest request = AnnouncePeerRequest.newRequest(token, infoHash);
		pushMessage(request, socketAddress);
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
		if(SUCCESS_VERIFY.apply(response)) {
		}
	}

	/**
	 * 等待响应
	 */
	private void waitResponse(Request request) {
		synchronized (request) {
			if(!request.response()) {
				ThreadUtils.wait(request, DhtConfig.TIMEOUT);
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
	private void pushMessage(Request request, InetSocketAddress socketAddress) {
		request.setSocketAddress(socketAddress);
		RequestManager.getInstance().put(request);
		final ByteBuffer buffer = ByteBuffer.wrap(request.toBytes());
		pushMessage(buffer, socketAddress);
	}
	
	/**
	 * 发送响应
	 */
	private void pushMessage(Response response, InetSocketAddress socketAddress) {
		final ByteBuffer buffer = ByteBuffer.wrap(response.toBytes());
		pushMessage(buffer, socketAddress);
	}
	
	/**
	 * 发送数据
	 */
	private void pushMessage(ByteBuffer buffer, InetSocketAddress socketAddress) {
		try {
			this.send(buffer, socketAddress);
		} catch (NetException e) {
			LOGGER.error("发送UDP消息异常", e);
		}
	}

}
