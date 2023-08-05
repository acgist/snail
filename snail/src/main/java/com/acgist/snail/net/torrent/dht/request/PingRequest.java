package com.acgist.snail.net.torrent.dht.request;

import com.acgist.snail.config.DhtConfig;
import com.acgist.snail.net.torrent.dht.DhtRequest;
import com.acgist.snail.net.torrent.dht.response.PingResponse;

/**
 * Ping
 * 
 * @author acgist
 */
public final class PingRequest extends DhtRequest {

    private PingRequest() {
        super(DhtConfig.QType.PING);
    }
    
    /**
     * 新建请求
     * 
     * @return 请求
     */
    public static final PingRequest newRequest() {
        return new PingRequest();
    }

    /**
     * 处理请求
     * 
     * @param request 请求
     * 
     * @return 响应
     */
    public static final PingResponse execute(DhtRequest request) {
        return PingResponse.newInstance(request);
    }
    
}
