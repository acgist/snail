package com.acgist.snail.net.upnp;

import java.net.InetSocketAddress;

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.context.wrapper.HeaderWrapper;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.NetException;
import com.acgist.snail.net.UdpClient;
import com.acgist.snail.utils.NetUtils;

/**
 * UPNP客户端
 * 
 * @author acgist
 */
public final class UpnpClient extends UdpClient<UpnpMessageHandler> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(UpnpClient.class);

    /**
     * M-SEARCH协议：{@value}
     */
    private static final String PROTOCOL = "M-SEARCH * HTTP/1.1";
    
    /**
     * @param socketAddress 地址
     */
    private UpnpClient(InetSocketAddress socketAddress) {
        super("UPNP Client", new UpnpMessageHandler(socketAddress));
    }
    
    public static final UpnpClient newInstance() {
        return new UpnpClient(NetUtils.buildSocketAddress(UpnpServer.upnpHost(), UpnpServer.UPNP_PORT));
    }
    
    @Override
    public boolean open() {
        return this.open(UpnpServer.getInstance().getChannel());
    }

    /**
     * 发送M-SEARCH消息
     */
    public void mSearch() {
        LOGGER.debug("发送M-SEARCH消息");
        try {
            this.send(this.buildMSearch());
        } catch (NetException e) {
            LOGGER.error("发送M-SEARCH消息异常", e);
        }
    }
    
    /**
     * 新建M-SEARCH消息
     * 
     * @return 消息
     */
    private String buildMSearch() {
        final HeaderWrapper builder = HeaderWrapper.newBuilder(PROTOCOL);
        builder
            .setHeader("HOST", SymbolConfig.Symbol.COLON.join(UpnpServer.upnpHost(), UpnpServer.UPNP_PORT))
            .setHeader("ST", UpnpServer.UPNP_ROOT_DEVICE)
            .setHeader("MAN", "\"ssdp:discover\"")
            .setHeader("MX", "3");
        return builder.build();
    }

}
