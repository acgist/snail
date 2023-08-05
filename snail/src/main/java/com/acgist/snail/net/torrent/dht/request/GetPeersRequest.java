package com.acgist.snail.net.torrent.dht.request;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;

import com.acgist.snail.config.DhtConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.torrent.TorrentContext;
import com.acgist.snail.net.torrent.TorrentSession;
import com.acgist.snail.net.torrent.dht.DhtRequest;
import com.acgist.snail.net.torrent.dht.NodeContext;
import com.acgist.snail.net.torrent.dht.NodeSession;
import com.acgist.snail.net.torrent.dht.response.GetPeersResponse;
import com.acgist.snail.net.torrent.peer.PeerContext;
import com.acgist.snail.net.torrent.peer.PeerSession;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * 查找Peer
 * 
 * @author acgist
 */
public final class GetPeersRequest extends DhtRequest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GetPeersRequest.class);

    private GetPeersRequest() {
        super(DhtConfig.QType.GET_PEERS);
    }
    
    /**
     * 新建请求
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
     * 处理请求
     * 尽量返回Peer否者返回最近Node节点
     * 
     * @param request 请求
     * 
     * @return 响应
     */
    public static final GetPeersResponse execute(DhtRequest request) {
        boolean needNodes                   = true;
        final GetPeersResponse response     = GetPeersResponse.newInstance(request);
        final byte[] infoHash               = request.getBytes(DhtConfig.KEY_INFO_HASH);
        final String infoHashHex            = StringUtils.hex(infoHash);
        final TorrentSession torrentSession = TorrentContext.getInstance().torrentSession(infoHashHex);
        if(torrentSession != null) {
            // TODO：IPv6
            final ByteBuffer buffer      = ByteBuffer.allocate(SystemConfig.IPV4_PORT_LENGTH);
            final List<PeerSession> list = PeerContext.getInstance().listPeerSession(infoHashHex);
            if(CollectionUtils.isNotEmpty(list)) {
                // 返回Peer
                needNodes = false;
                final List<byte[]> values = list.stream()
                    .filter(PeerSession::available)
                    .filter(PeerSession::connected)
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
        if(needNodes) {
            // 返回Node
            final List<NodeSession> nodes = NodeContext.getInstance().findNode(infoHash);
            // TODO：want
            response.put(DhtConfig.KEY_NODES, serializeNodes(nodes));
        }
        return response;
    }
    
}
