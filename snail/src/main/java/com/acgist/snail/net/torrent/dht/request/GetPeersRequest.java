package com.acgist.snail.net.torrent.dht.request;

import java.nio.ByteBuffer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.DhtConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.NodeContext;
import com.acgist.snail.context.PeerContext;
import com.acgist.snail.context.TorrentContext;
import com.acgist.snail.net.torrent.dht.DhtRequest;
import com.acgist.snail.net.torrent.dht.response.GetPeersResponse;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>查找Peer</p>
 * 
 * @author acgist
 */
public final class GetPeersRequest extends DhtRequest {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GetPeersRequest.class);

	private GetPeersRequest() {
		super(DhtConfig.QType.GET_PEERS);
	}
	
	/**
	 * <p>创建请求</p>
	 * 
	 * @param infoHash InfoHash
	 * 
	 * @return 请求
	 */
	public static final GetPeersRequest newRequest(byte[] infoHash) {
		final GetPeersRequest request = new GetPeersRequest();
		request.put(DhtConfig.KEY_INFO_HASH, infoHash);
		return request;
	}

	/**
	 * <p>处理请求</p>
	 * <p>能够查找到Peer返回Peer，反之返回最近的Node节点。</p>
	 * 
	 * @param request 请求
	 * 
	 * @return 响应
	 */
	public static final GetPeersResponse execute(DhtRequest request) {
		boolean needNodes = true;
		final GetPeersResponse response = GetPeersResponse.newInstance(request);
		final byte[] infoHash = request.getBytes(DhtConfig.KEY_INFO_HASH);
		final String infoHashHex = StringUtils.hex(infoHash);
		final TorrentSession torrentSession = TorrentContext.getInstance().torrentSession(infoHashHex);
		// 查找Peer
		if(torrentSession != null) {
			final ByteBuffer buffer = ByteBuffer.allocate(SystemConfig.IP_PORT_LENGTH);
			final var list = PeerContext.getInstance().listPeerSession(infoHashHex);
			if(CollectionUtils.isNotEmpty(list)) {
				// 返回Peer
				needNodes = false;
				final var values = list.stream()
					.filter(PeerSession::available) // 可用
					.filter(PeerSession::connected) // 连接
					.limit(DhtConfig.GET_PEER_SIZE)
					.map(peer -> {
						buffer.putInt(NetUtils.ipToInt(peer.host()));
						buffer.putShort(NetUtils.portToShort(peer.port()));
						buffer.flip();
						return buffer.array();
					})
					.collect(Collectors.toList());
				response.put(DhtConfig.KEY_VALUES, values);
			}
		} else {
			LOGGER.debug("查找Peer种子信息不存在：{}", infoHashHex);
		}
		// 没有Peer返回Node节点
		if(needNodes) {
			final var nodes = NodeContext.getInstance().findNode(infoHash);
			response.put(DhtConfig.KEY_NODES, serializeNodes(nodes));
		}
		return response;
	}
	
	/**
	 * <p>获取InfoHash</p>
	 * 
	 * @return InfoHash
	 */
	public byte[] getInfoHash() {
		return this.getBytes(DhtConfig.KEY_INFO_HASH);
	}

}
