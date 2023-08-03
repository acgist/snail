package com.acgist.snail.net.torrent.dht;

import java.util.Arrays;

import com.acgist.snail.utils.BeanUtils;

/**
 * DHT节点信息
 * 
 * @author acgist
 */
public final class NodeSession implements Comparable<NodeSession> {

    /**
     * DHT节点状态
     * 
     * @author acgist
     */
    public enum Status {
        
        /**
         * 未知：没有使用
         */
        UNUSE,
        /**
         * 验证：没有收到响应
         * 使用后标记为验证状态
         * 验证状态节点不能用于：保存、查找
         */
        VERIFY,
        /**
         * 可用：收到响应
         */
        AVAILABLE;
        
    }
    
    /**
     * 节点ID
     */
    private final byte[] id;
    /**
     * 节点地址
     */
    private final String host;
    /**
     * 节点端口
     */
    private final int port;
    /**
     * 节点状态
     */
    private Status status;

    /**
     * @param id   节点ID
     * @param host 节点地址
     * @param port 节点端口
     */
    private NodeSession(byte[] id, String host, int port) {
        this.id     = id;
        this.host   = host;
        this.port   = port;
        this.status = Status.UNUSE;
    }
    
    /**
     * 新建DHT节点信息
     * 
     * @param id   节点ID
     * @param host 节点地址
     * @param port 节点端口
     * 
     * @return {@link NodeSession}
     */
    public static final NodeSession newInstance(byte[] id, String host, int port) {
        return new NodeSession(id, host, port);
    }

    /**
     * 判断节点是否可以使用
     * 
     * @return 是否可以使用
     */
    public boolean useable() {
        return this.status != Status.VERIFY;
    }

    /**
     * 标记验证状态
     * 
     * @return 标记结果
     */
    public boolean markVerify() {
        if(this.status == Status.UNUSE) {
            this.status = Status.VERIFY;
        }
        return true;
    }
    
    /**
     * @return 节点ID
     */
    public byte[] getId() {
        return this.id;
    }

    /**
     * @return 节点地址
     */
    public String getHost() {
        return this.host;
    }

    /**
     * @return 节点端口
     */
    public int getPort() {
        return this.port;
    }

    /**
     * @return 节点状态
     */
    public Status getStatus() {
        return this.status;
    }

    /**
     * 设置节点状态
     * 
     * @param status 节点状态
     */
    public void setStatus(Status status) {
        this.status = status;
    }
    
    @Override
    public int compareTo(NodeSession target) {
        return Arrays.compareUnsigned(this.id, target.id);
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(this.id);
    }
    
    @Override
    public boolean equals(Object object) {
        if(this == object) {
            return true;
        }
        if(object instanceof NodeSession session) {
            return Arrays.equals(this.id, session.id);
        }
        return false;
    }
    
    @Override
    public String toString() {
        return BeanUtils.toString(this, this.id, this.host, this.port);
    }
    
}
