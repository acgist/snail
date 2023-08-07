package com.acgist.snail.net.torrent;

/**
 * Peer连接接口
 * 
 * @author acgist
 */
public interface IPeerConnect {

    /**
     * 连接类型
     * 
     * @author acgist
     */
    public enum ConnectType {

        /**
         * UTP
         */
        UTP,
        /**
         * TCP
         */
        TCP;
        
    }
    
    /**
     * @return 连接类型
     */
    ConnectType connectType();
    
}
