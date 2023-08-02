package com.acgist.snail.net.torrent.dht;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.acgist.snail.config.DhtConfig;
import com.acgist.snail.utils.BeanUtils;
import com.acgist.snail.utils.MapUtils;
import com.acgist.snail.utils.NetUtils;

/**
 * DHT消息
 * DHT请求、DHT响应
 *
 * @author acgist
 */
public abstract class DhtMessage {

    /**
     * 消息ID
     * 消息ID：请求ID=响应ID
     * 
     * @see DhtConfig#KEY_T
     */
    protected final byte[] t;
    /**
     * 消息类型
     * 
     * @see DhtConfig#KEY_Y
     * @see DhtConfig#KEY_Q
     * @see DhtConfig#KEY_R
     */
    protected final String y;
    /**
     * 地址：请求、响应
     */
    protected InetSocketAddress socketAddress;

    /**
     * @param t 消息ID
     * @param y 消息类型
     */
    protected DhtMessage(byte[] t, String y) {
        this.t = t;
        this.y = y;
    }

    /**
     * @return 消息ID
     */
    public byte[] getT() {
        return this.t;
    }

    /**
     * @return 消息类型
     */
    public String getY() {
        return this.y;
    }
    
    /**
     * @return 地址
     */
    public InetSocketAddress getSocketAddress() {
        return this.socketAddress;
    }

    /**
     * 设置地址
     * 
     * @param socketAddress 地址
     */
    public void setSocketAddress(InetSocketAddress socketAddress) {
        this.socketAddress = socketAddress;
    }
    
    /**
     * @return 是否是IPv4
     */
    protected boolean ipv4() {
        final String host = this.socketAddress.getHostString();
        return NetUtils.ipv4(host);
    }
    
    /**
     * @return 是否是IPv6
     */
    protected boolean ipv6() {
        final String host = this.socketAddress.getHostString();
        return NetUtils.ipv6(host);
    }
    
    /**
     * @return NodeId
     */
    public byte[] getNodeId() {
        return this.getBytes(DhtConfig.KEY_ID);
    }
    
    /**
     * @param key 参数名称
     * 
     * @return Integer参数
     */
    public Integer getInteger(String key) {
        return MapUtils.getInteger(this.get(), key);
    }
    
    /**
     * @param key 参数名称
     * 
     * @return Long参数
     */
    public Long getLong(String key) {
        return MapUtils.getLong(this.get(), key);
    }
    
    /**
     * @param key 参数名称
     * 
     * @return 字符串参数
     */
    public String getString(String key) {
        return MapUtils.getString(this.get(), key);
    }
    
    /**
     * @param key 参数名称
     * 
     * @return byte[]参数
     */
    public byte[] getBytes(String key) {
        return MapUtils.getBytes(this.get(), key);
    }
    
    /**
     * @param key 参数名称
     * 
     * @return List参数
     */
    public List<Object> getList(String key) {
        return MapUtils.getList(this.get(), key);
    }
    
    /**
     * @param key 参数名称
     * 
     * @return Object参数
     */
    public Object get(String key) {
        return MapUtils.get(this.get(), key);
    }
    
    /**
     * @return 参数
     */
    protected abstract Map<String, Object> get();
    
    /**
     * 设置参数
     * 
     * @param key   参数名称
     * @param value 参数值
     */
    public abstract void put(String key, Object value);
    
    /**
     * 将消息转为B编码的字节数组
     * 
     * @return B编码的字节数组
     */
    public abstract byte[] toBytes();
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(this.t);
    }
    
    @Override
    public boolean equals(Object object) {
        if(this == object) {
            return true;
        }
        if(object instanceof DhtMessage message) {
            return Arrays.equals(this.t, message.t);
        }
        return false;
    }
    
    @Override
    public String toString() {
        return BeanUtils.toString(this, this.t, this.y);
    }
    
}
