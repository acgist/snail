package com.acgist.snail.net.torrent.dht;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.function.Predicate;

import com.acgist.snail.config.DhtConfig;
import com.acgist.snail.config.DhtConfig.ErrorCode;
import com.acgist.snail.config.DhtConfig.QType;
import com.acgist.snail.format.BEncodeDecoder;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.NetException;
import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.torrent.TorrentContext;
import com.acgist.snail.net.torrent.TorrentSession;
import com.acgist.snail.net.torrent.dht.request.AnnouncePeerRequest;
import com.acgist.snail.net.torrent.dht.request.FindNodeRequest;
import com.acgist.snail.net.torrent.dht.request.GetPeersRequest;
import com.acgist.snail.net.torrent.dht.request.PingRequest;
import com.acgist.snail.net.torrent.dht.response.FindNodeResponse;
import com.acgist.snail.net.torrent.dht.response.GetPeersResponse;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>DHT消息代理</p>
 * <p>DHT Protocol</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0005.html</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0032.html</p>
 * 
 * @author acgist
 */
public final class DhtMessageHandler extends UdpMessageHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DhtMessageHandler.class);
	
	/**
	 * <p>判断响应是否成功</p>
	 */
	private static final Predicate<DhtResponse> RESPONSE_SUCCESS = response -> response != null && response.success();
	
	/**
	 * <p>服务端</p>
	 */
	public DhtMessageHandler() {
		this(null);
	}
	
	/**
	 * <p>客户端</p>
	 * 
	 * @param socketAddress 地址
	 */
	public DhtMessageHandler(InetSocketAddress socketAddress) {
		super(socketAddress);
	}
	
	@Override
	public void onReceive(ByteBuffer buffer, InetSocketAddress socketAddress) throws NetException {
		final var decoder = BEncodeDecoder.newInstance(buffer).next();
		if(decoder.isEmpty()) {
			LOGGER.warn("处理DHT消息错误（格式）：{}", decoder);
			return;
		}
		final String y = decoder.getString(DhtConfig.KEY_Y);
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
	 * @param socketAddress 地址
	 */
	private void onRequest(final DhtRequest request, final InetSocketAddress socketAddress) {
		DhtResponse response;
		final QType type = request.getQ();
		if(type == null) {
			LOGGER.warn("处理DHT请求失败（未知类型）：{}", type);
			response = DhtResponse.buildErrorResponse(request.getT(), ErrorCode.CODE_204, "不支持的请求类型");
		} else {
			LOGGER.debug("处理DHT请求：{}", type);
			response = switch (type) {
				case PING -> this.ping(request);
				case FIND_NODE -> this.findNode(request);
				case GET_PEERS -> this.getPeers(request);
				case ANNOUNCE_PEER -> this.announcePeer(request);
				default -> {
					LOGGER.warn("处理DHT请求失败（类型未适配）：{}", type);
					yield DhtResponse.buildErrorResponse(request.getT(), ErrorCode.CODE_202, "未适配的请求类型");
				}
			};
		}
		this.pushMessage(response, socketAddress);
	}
	
	/**
	 * <p>处理响应</p>
	 * 
	 * @param response 响应
	 */
	private void onResponse(final DhtResponse response) {
		final DhtRequest request = DhtContext.getInstance().response(response);
		if(request == null) {
			LOGGER.warn("处理DHT响应失败：没有对应请求");
			return;
		}
		final QType type = request.getQ();
		if(type == null) {
			LOGGER.warn("处理DHT响应失败（未知类型）：{}", type);
			return;
		}
		if(!RESPONSE_SUCCESS.test(response)) {
			LOGGER.warn("处理DHT响应失败（失败响应）：{}", response);
			return;
		}
		LOGGER.debug("处理DHT响应：{}", type);
		switch (type) {
			case PING -> this.ping(request, response);
			case FIND_NODE -> this.findNode(request, response);
			case GET_PEERS -> this.getPeers(request, response);
			case ANNOUNCE_PEER -> this.announcePeer(request, response);
			default -> LOGGER.warn("处理DHT响应失败（类型未适配）：{}", type);
		}
	}
	
	/**
	 * <p>发送请求：ping</p>
	 * <p>检测节点是否可达，该方法同步阻塞，收到响应后添加系统节点。</p>
	 * 
	 * @return 节点信息
	 */
	public NodeSession ping() {
		LOGGER.debug("发送DHT请求：ping");
		final PingRequest request = PingRequest.newRequest();
		this.pushRequest(request, this.socketAddress);
		request.lockResponse();
		final DhtResponse response = request.getResponse();
		if(RESPONSE_SUCCESS.test(response)) {
			return NodeContext.getInstance().newNodeSession(response.getNodeId(), this.socketAddress.getHostString(), this.socketAddress.getPort());
		} else {
			LOGGER.warn("发送Ping请求失败：{}-{}", this.socketAddress, response);
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
	 * @param target NodeId或者InfoHash
	 */
	public void findNode(byte[] target) {
		LOGGER.debug("发送DHT请求：findNode");
		final FindNodeRequest request = FindNodeRequest.newRequest(target);
		this.pushRequest(request, this.socketAddress);
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
	 * @param infoHash InfoHash
	 */
	public void getPeers(byte[] infoHash) {
		LOGGER.debug("发送DHT请求：getPeers");
		final GetPeersRequest request = GetPeersRequest.newRequest(infoHash);
		this.pushRequest(request, this.socketAddress);
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
		if(getPeersResponse.hasPeers()) {
			getPeersResponse.getPeers(infoHashHex);
		}
		if(getPeersResponse.hasNodes()) {
			getPeersResponse.getNodes();
		}
		final byte[] token = getPeersResponse.getToken();
		if(token != null) {
			final TorrentSession torrentSession = TorrentContext.getInstance().torrentSession(infoHashHex);
			if(torrentSession != null && torrentSession.uploadable()) {
				this.announcePeer(token, infoHash, request.getSocketAddress());
			}
		}
	}
	
	/**
	 * <p>发送请求：announcePeer</p>
	 * 
	 * @param token Token
	 * @param infoHash InfoHash
	 */
	public void announcePeer(byte[] token, byte[] infoHash) {
		this.announcePeer(token, infoHash, this.socketAddress);
	}
	
	/**
	 * <p>发送请求：announcePeer</p>
	 * 
	 * @param token Token
	 * @param infoHash InfoHash
	 * @param socketAddress 地址
	 */
	private void announcePeer(byte[] token, byte[] infoHash, InetSocketAddress socketAddress) {
		LOGGER.debug("发送DHT请求：announcePeer");
		final AnnouncePeerRequest request = AnnouncePeerRequest.newRequest(token, infoHash);
		this.pushRequest(request, socketAddress);
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
		LOGGER.debug("处理DHT响应：AnnouncePeer");
	}

	/**
	 * <p>发送DHT请求</p>
	 * 
	 * @param request 请求
	 * @param socketAddress 地址
	 */
	private void pushRequest(DhtRequest request, InetSocketAddress socketAddress) {
		request.setSocketAddress(socketAddress);
		DhtContext.getInstance().request(request);
		this.pushMessage(request, socketAddress);
	}
	
	/**
	 * <p>发送DHT消息</p>
	 * 
	 * @param message 消息
	 * @param socketAddress 地址
	 */
	private void pushMessage(DhtMessage message, InetSocketAddress socketAddress) {
		final ByteBuffer buffer = ByteBuffer.wrap(message.toBytes());
		try {
			this.send(buffer, socketAddress);
		} catch (NetException e) {
			LOGGER.error("DHT消息发送异常", e);
		}
	}

}
