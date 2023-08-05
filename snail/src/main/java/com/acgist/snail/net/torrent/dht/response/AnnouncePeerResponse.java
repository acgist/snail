package com.acgist.snail.net.torrent.dht.response;

import com.acgist.snail.net.torrent.dht.DhtRequest;
import com.acgist.snail.net.torrent.dht.DhtResponse;

/**
 * 声明Peer
 * 
 * @author acgist
 */
public final class AnnouncePeerResponse extends DhtResponse {

    /**
     * @param t 节点ID
     */
    private AnnouncePeerResponse(byte[] t) {
        super(t);
    }
    
    /**
     * @param response 响应
     */
    private AnnouncePeerResponse(DhtResponse response) {
        super(response.getT(), response.getY(), response.getR(), response.getE());
    }
    
    /**
     * 新建响应
     * 
     * @param request 请求
     * 
     * @return 响应
     */
    public static final AnnouncePeerResponse newInstance(DhtRequest request) {
        return new AnnouncePeerResponse(request.getT());
    }

    /**
     * 新建响应
     * 
     * @param response 响应
     * 
     * @return 响应
     */
    public static final AnnouncePeerResponse newInstance(DhtResponse response) {
        return new AnnouncePeerResponse(response);
    }
    
}
