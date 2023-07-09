package com.acgist.snail.net.application;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.net.TcpServer;

/**
 * 系统服务端
 * 
 * @author acgist
 */
public final class ApplicationServer extends TcpServer<ApplicationMessageHandler> {

    private static final ApplicationServer INSTANCE = new ApplicationServer();
    
    public static final ApplicationServer getInstance() {
        return INSTANCE;
    }
    
    private ApplicationServer() {
        super("Application Server", ApplicationMessageHandler.class);
    }
    
    @Override
    public boolean listen() {
        return this.listen(SystemConfig.getServicePort());
    }
    
}