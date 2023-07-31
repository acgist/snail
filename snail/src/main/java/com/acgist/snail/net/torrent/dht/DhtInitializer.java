package com.acgist.snail.net.torrent.dht;

import com.acgist.snail.config.DhtConfig;
import com.acgist.snail.context.Initializer;

/**
 * DHT初始化器
 * 
 * @author acgist
 */
public final class DhtInitializer extends Initializer {

    private DhtInitializer() {
        super("DHT");
    }

    public static final DhtInitializer newInstance() {
        return new DhtInitializer();
    }

    @Override
    protected void init() {
        DhtConfig.getInstance();
        DhtContext.getInstance();
        NodeContext.getInstance();
    }
    
    @Override
    protected void release() {
        DhtConfig.getInstance().persistent();
    }

}
