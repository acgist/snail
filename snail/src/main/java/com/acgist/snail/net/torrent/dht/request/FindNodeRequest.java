package com.acgist.snail.net.torrent.dht.request;

import java.util.List;

import com.acgist.snail.config.DhtConfig;
import com.acgist.snail.net.torrent.dht.DhtRequest;
import com.acgist.snail.net.torrent.dht.NodeContext;
import com.acgist.snail.net.torrent.dht.NodeSession;
import com.acgist.snail.net.torrent.dht.response.FindNodeResponse;

/**
 * 查找Node
 * 
 * @author acgist
 */
public final class FindNodeRequest extends DhtRequest {

    private FindNodeRequest() {
        super(DhtConfig.QType.FIND_NODE);
    }
    
    /**
     * 新建请求
     * 
     * @param target NodeId或者InfoHash
     * 
     * @return 请求
     */
    public static final FindNodeRequest newRequest(byte[] target) {
        final FindNodeRequest request = new FindNodeRequest();
        request.put(DhtConfig.KEY_TARGET, target);
        return request;
    }

    /**
     * 处理请求
     * 
     * @param request 请求
     * 
     * @return 响应
     */
    public static final FindNodeResponse execute(DhtRequest request) {
        final FindNodeResponse response = FindNodeResponse.newInstance(request);
        final byte[] target             = request.getBytes(DhtConfig.KEY_TARGET);
        final List<NodeSession> nodes   = NodeContext.getInstance().findNode(target);
        // TODO：want
        response.put(DhtConfig.KEY_NODES, serializeNodes(nodes));
        return response;
    }
    
}
