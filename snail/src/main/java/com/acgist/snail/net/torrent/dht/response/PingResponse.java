package com.acgist.snail.net.torrent.dht.response;

import com.acgist.snail.net.torrent.dht.DhtRequest;
import com.acgist.snail.net.torrent.dht.DhtResponse;

/**
 * Ping
 * 
 * @author acgist
 */
public final class PingResponse extends DhtResponse {
    
    /**
     * @param t 节点ID
     */
    private PingResponse(byte[] t) {
        super(t);
    }

    /**
     * @param response 响应
     */
    private PingResponse(DhtResponse response) {
        super(response.getT(), response.getY(), response.getR(), response.getE());
    }
    
    /**
     * 新建响应
     * 
     * @param request 请求
     * 
     * @return 响应
     */
    public static final PingResponse newInstance(DhtRequest request) {
        return new PingResponse(request.getT());
    }

    /**
     * 新建响应
     * 
     * @param response 响应
     * 
     * @return 响应
     */
    public static final PingResponse newInstance(DhtResponse response) {
        return new PingResponse(response);
    }
    
}
