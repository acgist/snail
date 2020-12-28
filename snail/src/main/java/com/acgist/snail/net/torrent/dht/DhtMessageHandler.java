package com.acgist.snail.net.torrent.dht;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.DhtConfig;
import com.acgist.snail.config.DhtConfig.ErrorCode;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.format.BEncodeDecoder;
import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.dht.request.AnnouncePeerRequest;
import com.acgist.snail.net.torrent.dht.request.FindNodeRequest;
import com.acgist.snail.net.torrent.dht.request.GetPeersRequest;
import com.acgist.snail.net.torrent.dht.request.PingRequest;
import com.acgist.snail.net.torrent.dht.response.FindNodeResponse;
import com.acgist.snail.net.torrent.dht.response.GetPeersResponse;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>DHT消息代理</p>
 * <p>DHT Protocol</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0005.html</p>
 * 
 * @author acgist
 */
public final class DhtMessageHandler extends UdpMessageHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DhtMessageHandler.class);
	
	/**
	 * <p>判断响应是否成功</p>
	 */
	public static final Function<DhtResponse, Boolean> RESPONSE_SUCCESS = (response) -> {
		return response != null && response.success();
	};
	
	@Override
	public void onReceive(ByteBuffer buffer, InetSocketAddress socketAddress) throws NetException {
		buffer.flip();
		final var decoder = BEncodeDecoder.newInstance(buffer);
		decoder.nextMap();
		if(decoder.isEmpty()) {
			LOGGER.warn("处理DHT消息错误（格式）：{}", decoder.oddString());
			return;
		}
		final String y = decoder.getString(DhtConfig.KEY_Y); // 消息类型
		if(DhtConfig.KEY_Q.equals(y)) {
			final DhtRequest request = DhtRequest.valueOf(decoder);
			request.setSocketAddress(socketAddress);
			this.onRequest(request, socketAddress);
		} else if(DhtConfig.KEY_R.equals(y)) {
			final DhtResponse response = DhtResponse.valueOf(decoder);
			response.setSocketAddress(socketAddress);
			this.onResponse(response);
		} else {
			LOGGER.warn("处理DHT消息错误（未知类型）：{}", y);
		}
	}
	
	/**
	 * <p>处理请求</p>
	 * 
	 * @param request 请求
	 * @param socketAddress 客户端地址
	 */
	private void onRequest(final DhtRequest request, final InetSocketAddress socketAddress) {
		DhtResponse response = null;
		if(request.getQ() == null) {
			LOGGER.warn("处理DHT请求失败（未知类型）：{}", request.getQ());
			response = DhtResponse.buildErrorResponse(request.getT(), ErrorCode.CODE_204.code(), "不支持的请求类型");
		} else {
			LOGGER.debug("处理DHT请求：{}", request.getQ());
			switch (request.getQ()) {
			case PING:
				response = this.ping(request);
				break;
			case FIND_NODE:
				response = this.findNode(request);
				break;
			case GET_PEERS:
				response = this.getPeers(request);
				break;
			case ANNOUNCE_PEER:
				response = this.announcePeer(request);
				break;
			default:
				LOGGER.warn("处理DHT请求失败（类型未适配）：{}", request.getQ());
				response = DhtResponse.buildErrorResponse(request.getT(), ErrorCode.CODE_202.code(), "未适配的请求类型");
				break;
			}
		}
		this.pushMessage(response, socketAddress);
	}
	
	/**
	 * <p>处理响应</p>
	 * 
	 * @param response 响应
	 */
	private void onResponse(final DhtResponse response) {
		final DhtRequest request = DhtManager.getInstance().response(response);
		if(request == null) {
			LOGGER.warn("处理DHT响应失败：没有对应的请求");
			return;
		}
		if(request.getQ() == null) {
			LOGGER.warn("处理DHT响应失败（未知类型）：{}", request.getQ());
			return;
		}
		LOGGER.debug("处理DHT响应：{}", request.getQ());
		if(!RESPONSE_SUCCESS.apply(response)) {
			LOGGER.warn("处理DHT响应失败（失败响应）：{}", response);
			return;
		}
		switch (request.getQ()) {
		case PING:
			this.ping(request, response);
			break;
		case FIND_NODE:
			this.findNode(request, response);
			break;
		case GET_PEERS:
			this.getPeers(request, response);
			break;
		case ANNOUNCE_PEER:
			this.announcePeer(request, response);
			break;
		default:
			LOGGER.warn("处理DHT响应失败（类型未适配）：{}", request.getQ());
			break;
		}
	}
	
	/**
	 * <p>发送请求：ping</p>
	 * <p>检测节点是否可达，该方法同步阻塞，收到响应后添加系统节点。</p>
	 * 
	 * @param socketAddress 地址
	 * 
	 * @return 节点信息
	 */
	public NodeSession ping(InetSocketAddress socketAddress) {
		LOGGER.debug("发送DHT请求：ping");
		final PingRequest request = PingRequest.newRequest();
		this.pushMessage(request, socketAddress);
		request.lockResponse();
		final DhtResponse response = request.getResponse();
		if(RESPONSE_SUCCESS.apply(response)) {
			final NodeSession nodeSession = NodeManager.getInstance().newNodeSession(response.getNodeId(), socketAddress.getHostString(), socketAddress.getPort());
			NodeManager.getInstance().sortNodes();
			return nodeSession;
		} else {
			LOGGER.warn("发送Ping请求失败：{}-{}", socketAddress, response);
		}
		return null;
	}

	/**
	 * <p>处理请求：ping</p>
	 * 
	 * @param request 请求
	 * 
	 * @return 响应
	 */
	private DhtResponse ping(DhtRequest request) {
		return PingRequest.execute(request);
	}

	/**
	 * <p>处理响应：ping</p>
	 * 
	 * @param request 请求
	 * @param response 响应
	 */
	private void ping(DhtRequest request, DhtResponse response) {
		request.unlockResponse();
	}
	
	/**
	 * <p>发送请求：findNode</p>
	 * 
	 * @param socketAddress 地址
	 * @param target NodeId或者InfoHash
	 */
	public void findNode(InetSocketAddress socketAddress, byte[] target) {
		LOGGER.debug("发送DHT请求：findNode");
		final FindNodeRequest request = FindNodeRequest.newRequest(target);
		this.pushMessage(request, socketAddress);
	}
	
	/**
	 * <p>处理请求：findNode</p>
	 * 
	 * @param request 请求
	 * 
	 * @return 响应
	 */
	private DhtResponse findNode(DhtRequest request) {
		return FindNodeRequest.execute(request);
	}
	
	/**
	 * <p>处理响应：findNode</p>
	 * 
	 * @param request 请求
	 * @param response 响应
	 */
	private void findNode(DhtRequest request, DhtResponse response) {
		FindNodeResponse.newInstance(response).getNodes();
	}

	/**
	 * <p>发送请求：getPeers</p>
	 * 
	 * @param socketAddress 地址
	 * @param infoHash InfoHash
	 */
	public void getPeers(InetSocketAddress socketAddress, byte[] infoHash) {
		LOGGER.debug("发送DHT请求：getPeers");
		final GetPeersRequest request = GetPeersRequest.newRequest(infoHash);
		this.pushMessage(request, socketAddress);
	}

	/**
	 * <p>处理请求：getPeers</p>
	 * 
	 * @param request 请求
	 * 
	 * @return 响应
	 */
	private DhtResponse getPeers(DhtRequest request) {
		return GetPeersRequest.execute(request);
	}

	/**
	 * <p>处理响应：getPeers</p>
	 * <p>处理完成后如果也在下载同一个BT任务发送声明消息</p>
	 * 
	 * @param request 请求
	 * @param response 响应
	 */
	private void getPeers(DhtRequest request, DhtResponse response) {
		final byte[] infoHash = request.getBytes(DhtConfig.KEY_INFO_HASH);
		final String infoHashHex = StringUtils.hex(infoHash);
		final GetPeersResponse getPeersResponse = GetPeersResponse.newInstance(response);
		// 处理Peer
		if(getPeersResponse.hasPeers()) {
			getPeersResponse.getPeers(infoHashHex);
		}
		// 处理节点
		if(getPeersResponse.hasNodes()) {
			getPeersResponse.getNodes();
		}
		// 发送声明消息
		final byte[] token = getPeersResponse.getToken();
		if(token != null) {
			final TorrentSession torrentSession = TorrentManager.getInstance().torrentSession(infoHashHex);
			if(torrentSession != null && torrentSession.uploadable()) {
				this.announcePeer(request.getSocketAddress(), token, infoHash);
			}
		}
	}
	
	/**
	 * <p>发送请求：announcePeer</p>
	 * 
	 * @param socketAddress 地址
	 * @param token Token
	 * @param infoHash InfoHash
	 */
	public void announcePeer(InetSocketAddress socketAddress, byte[] token, byte[] infoHash) {
		LOGGER.debug("发送DHT请求：announcePeer");
		final AnnouncePeerRequest request = AnnouncePeerRequest.newRequest(token, infoHash);
		this.pushMessage(request, socketAddress);
	}
	
	/**
	 * <p>处理请求：announcePeer</p>
	 * 
	 * @param request 请求
	 * 
	 * @return 响应
	 */
	private DhtResponse announcePeer(DhtRequest request) {
		return AnnouncePeerRequest.execute(request);
	}

	/**
	 * <p>处理响应：announcePeer</p>
	 * 
	 * @param request 请求
	 * @param response 响应
	 */
	private void announcePeer(DhtRequest request, DhtResponse response) {
	}

	/**
	 * <p>发送请求</p>
	 * 
	 * @param request 请求
	 * @param socketAddress 地址
	 */
	private void pushMessage(DhtRequest request, InetSocketAddress socketAddress) {
		request.setSocketAddress(socketAddress);
		DhtManager.getInstance().request(request);
		final ByteBuffer buffer = ByteBuffer.wrap(request.toBytes());
		this.pushMessage(buffer, socketAddress);
	}
	
	/**
	 * <p>发送响应</p>
	 * 
	 * @param response 响应
	 * @param socketAddress 地址
	 */
	private void pushMessage(DhtResponse response, InetSocketAddress socketAddress) {
		if(response != null) {
			final ByteBuffer buffer = ByteBuffer.wrap(response.toBytes());
			this.pushMessage(buffer, socketAddress);
		}
	}
	
	/**
	 * <p>发送数据</p>
	 * 
	 * @param buffer 消息
	 * @param socketAddress 地址
	 */
	private void pushMessage(ByteBuffer buffer, InetSocketAddress socketAddress) {
		try {
			this.send(buffer, socketAddress);
		} catch (NetException e) {
			LOGGER.error("DHT消息发送异常", e);
		}
	}

}
