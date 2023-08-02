package com.acgist.snail.net.torrent.dht;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.acgist.snail.config.DhtConfig;
import com.acgist.snail.config.DhtConfig.QType;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.format.BEncodeDecoder;
import com.acgist.snail.format.BEncodeEncoder;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.utils.BeanUtils;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.NetUtils;

/**
 * DHT请求
 * 
 * @author acgist
 */
public class DhtRequest extends DhtMessage {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DhtRequest.class);

    /**
     * 请求类型
     * 
     * @see DhtConfig#KEY_Q
     */
    private final DhtConfig.QType q;
    /**
     * 请求参数
     * 
     * @see DhtConfig#KEY_A
     */
    private final Map<String, Object> a;
    /**
     * 请求时间戳
     */
    private final long timestamp;
    /**
     * 响应
     */
    private DhtResponse response;
    
    /**
     * 新建请求
     * 
     * @param q 请求类型
     */
    protected DhtRequest(DhtConfig.QType q) {
        this(DhtContext.getInstance().buildRequestId(), DhtConfig.KEY_Q, q, new LinkedHashMap<>());
        this.put(DhtConfig.KEY_ID, NodeContext.getInstance().nodeId());
    }
    
    /**
     * 解析请求
     * 
     * @param t 消息ID
     * @param y 消息类型
     * @param q 请求类型
     * @param a 请求参数
     */
    private DhtRequest(byte[] t, String y, DhtConfig.QType q, Map<String, Object> a) {
        super(t, y);
        this.q = q;
        this.a = a;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 读取请求
     * 
     * @param decoder 消息
     * 
     * @return 请求
     */
    public static final DhtRequest valueOf(final BEncodeDecoder decoder) {
        final byte[] t = decoder.getBytes(DhtConfig.KEY_T);
        final String y = decoder.getString(DhtConfig.KEY_Y);
        final QType q  = DhtConfig.QType.of(decoder.getString(DhtConfig.KEY_Q));
        final Map<String, Object> a = decoder.getMap(DhtConfig.KEY_A);
        return new DhtRequest(t, y, q, a);
    }
    
    /**
     * @return 请求类型
     */
    public QType getQ() {
        return this.q;
    }

    /**
     * @return 请求参数
     */
    public Map<String, Object> getA() {
        return this.a;
    }
    
    /**
     * @return 请求时间戳
     */
    public long getTimestamp() {
        return this.timestamp;
    }

    /**
     * @return 响应
     */
    public DhtResponse getResponse() {
        return this.response;
    }

    /**
     * 设置响应
     * 
     * @param response 响应
     */
    public void setResponse(DhtResponse response) {
        this.response = response;
    }

    /**
     * @return 是否已经获取响应
     */
    public boolean hasResponse() {
        return this.response != null;
    }

    @Override
    public final Map<String, Object> get() {
        return this.a;
    }
    
    @Override
    public final void put(String key, Object value) {
        this.a.put(key, value);
    }
    
    @Override
    public final byte[] toBytes() {
        final Map<String, Object> request = new LinkedHashMap<>();
        request.put(DhtConfig.KEY_T, this.t);
        request.put(DhtConfig.KEY_Y, this.y);
        request.put(DhtConfig.KEY_Q, this.q.getValue());
        request.put(DhtConfig.KEY_A, this.a);
        return BEncodeEncoder.encodeMap(request);
    }
    
    /**
     * @return 是否支持IPv4
     */
    protected boolean nodesIPv4() {
        final List<?> want = this.getList(DhtConfig.KEY_WANT);
        return this.nodesContain(want, DhtConfig.KEY_WANT_N4);
    }
    
    /**
     * @return 是否支持IPv6
     */
    protected boolean nodesIPv6() {
        final List<?> want = this.getList(DhtConfig.KEY_WANT);
        return this.nodesContain(want, DhtConfig.KEY_WANT_N6);
    }
    
    /**
     * 判断集合是否包含数据
     * 
     * @param list  集合
     * @param value 数据
     * 
     * @return 是否包含
     */
    private boolean nodesContain(List<?> list, String value) {
        if(CollectionUtils.isEmpty(list)) {
            return false;
        }
        for (Object object : list) {
            if(
                object instanceof byte[] bytes &&
                value.equals(new String(bytes))
            ) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 序列化节点列表
     * 
     * @param nodes 节点列表
     * 
     * @return 节点数据
     */
    protected static final byte[] serializeNodes(List<NodeSession> nodes) {
        if(CollectionUtils.isEmpty(nodes)) {
            return new byte[0];
        }
        final List<NodeSession> availableNodes = nodes.stream()
            // 分享IP地址
            .filter(node -> NetUtils.ip(node.getHost()))
            .collect(Collectors.toList());
        if(CollectionUtils.isEmpty(availableNodes)) {
            return new byte[0];
        }
        // TODO：IPv6
        final ByteBuffer buffer = ByteBuffer.allocate(26 * availableNodes.size());
        for (NodeSession node : availableNodes) {
            buffer.put(node.getId());
            buffer.putInt(NetUtils.ipToInt(node.getHost()));
            buffer.putShort(NetUtils.portToShort(node.getPort()));
        }
        return buffer.array();
    }
    
    /**
     * 添加响应锁
     */
    public void lockResponse() {
        if(!this.hasResponse()) {
            synchronized (this) {
                if(!this.hasResponse()) {
                    try {
                        this.wait(SystemConfig.RECEIVE_TIMEOUT_MILLIS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        LOGGER.debug("线程等待异常", e);
                    }
                }
            }
        }
    }
    
    /**
     * 释放响应锁
     */
    public void unlockResponse() {
        synchronized (this) {
            this.notifyAll();
        }
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(this.t);
    }
    
    @Override
    public boolean equals(Object object) {
        if(this == object) {
            return true;
        }
        if(object instanceof DhtRequest request) {
            return Arrays.equals(this.t, request.t);
        }
        return false;
    }
    
    @Override
    public String toString() {
        return BeanUtils.toString(this, this.t, this.y, this.q, this.a);
    }
    
}
