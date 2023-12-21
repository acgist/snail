package com.acgist.snail.net.upnp;

import com.acgist.snail.net.UdpServer;
import com.acgist.snail.utils.NetUtils;

/**
 * UPNP服务端
 * 
 * @author acgist
 */
public final class UpnpServer extends UdpServer<UpnpAcceptHandler> {

    private static final UpnpServer INSTANCE = new UpnpServer();
    
    public static final UpnpServer getInstance() {
        return INSTANCE;
    }
    
    /**
     * UPNP组播端口：{@value}
     */
    public static final int UPNP_PORT = 1900;
    /**
     * UPNP组播地址（IPv4）：{@value}
     */
    private static final String UPNP_HOST = "239.255.255.250";
    /**
     * UPNP组播地址（IPv6）：{@value}
     */
    private static final String UPNP_HOST_IPV6 = "[ff15::efff:fffa]";
    /**
     * UPNP根设备：{@value}
     */
    public static final String UPNP_ROOT_DEVICE = "upnp:rootdevice";
    
    private UpnpServer() {
        // 不监听UPNP端口：防止收到其他应用消息
        super("UPNP Server", UpnpAcceptHandler.getInstance());
        this.join(TTL, upnpHost());
        this.handle();
    }

    /**
     * @return UPNP组播地址
     */
    public static final String upnpHost() {
        if(NetUtils.localIPv4()) {
            return UPNP_HOST;
        } else {
            return UPNP_HOST_IPV6;
        }
    }
    
}
