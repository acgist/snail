package com.acgist.snail.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.torrent.TorrentAcceptHandler;
import com.acgist.snail.net.torrent.dht.DhtContext;
import com.acgist.snail.net.torrent.dht.NodeContext;
import com.acgist.snail.net.torrent.dht.NodeSession;
import com.acgist.snail.net.torrent.dht.request.AnnouncePeerRequest;
import com.acgist.snail.net.torrent.dht.request.FindNodeRequest;
import com.acgist.snail.net.torrent.dht.request.GetPeersRequest;
import com.acgist.snail.net.torrent.dht.request.PingRequest;
import com.acgist.snail.net.torrent.dht.response.AnnouncePeerResponse;
import com.acgist.snail.net.torrent.dht.response.FindNodeResponse;
import com.acgist.snail.net.torrent.dht.response.GetPeersResponse;
import com.acgist.snail.net.torrent.dht.response.PingResponse;
import com.acgist.snail.utils.StringUtils;

/**
 * DHT配置
 * 
 * @author acgist
 */
public final class DhtConfig extends PropertiesConfig {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DhtConfig.class);
    
    /**
     * 请求类型
     * 
     * @author acgist
     */
    public enum QType {
        
        /**
         * ping
         * 
         * @see PingRequest
         * @see PingResponse
         */
        PING("ping"),
        /**
         * 查找节点
         * 
         * @see FindNodeRequest
         * @see FindNodeResponse
         */
        FIND_NODE("find_node"),
        /**
         * 查找Peer
         * 
         * @see GetPeersRequest
         * @see GetPeersResponse
         */
        GET_PEERS("get_peers"),
        /**
         * 声明Peer
         * 
         * @see AnnouncePeerRequest
         * @see AnnouncePeerResponse
         */
        ANNOUNCE_PEER("announce_peer");
        
        /**
         * 类型标识
         */
        private final String value;
        
        /**
         * @param value 类型标识
         */
        private QType(String value) {
            this.value = value;
        }
        
        /**
         * @return 类型标识
         */
        public String getValue() {
            return this.value;
        }
        
        /**
         * @param value 类型标识
         * 
         * @return 请求类型
         */
        public static final QType of(String value) {
            final QType[] types = QType.values();
            for (final QType type : types) {
                if(type.value.equals(value)) {
                    return type;
                }
            }
            return null;
        }
        
    }
    
    /**
     * 错误编码
     * 数据类型：{@link List}
     * 数据格式：[0]=错误编码；[1]=错误描述；
     * 
     * @author acgist
     */
    public enum ErrorCode {
        
        /**
         * 一般错误
         */
        CODE_201(201),
        /**
         * 服务错误
         */
        CODE_202(202),
        /**
         * 协议错误：不规范包、无效参数、错误Token
         */
        CODE_203(203),
        /**
         * 未知方法
         */
        CODE_204(204);
        
        /**
         * 错误编码
         */
        private final int code;
        
        /**
         * @param code 错误编码
         */
        private ErrorCode(int code) {
            this.code = code;
        }
        
        /**
         * @return 错误编码
         */
        public int getCode() {
            return this.code;
        }
        
    }
    
    /**
     * DHT配置文件
     */
    public static final String DHT_CONFIG = "/config/bt.dht.properties";
    /**
     * DHT消息字符
     * 
     * @see TorrentAcceptHandler
     */
    public static final byte DHT_HEADER = 'd';
    /**
     * 消息ID（请求ID、响应ID）
     * 
     * @see DhtContext#buildRequestId()
     */
    public static final String KEY_T = "t";
    /**
     * 消息类型
     * 请求消息类型：{@link #KEY_Q}
     * 响应消息类型：{@link #KEY_R}
     */
    public static final String KEY_Y = "y";
    /**
     * 请求消息类型、请求类型
     * 请求消息类型：{@link #KEY_Y}
     * 请求类型：{@link QType}
     */
    public static final String KEY_Q = "q";
    /**
     * 响应消息类型、响应参数
     * 响应消息类型：{@link #KEY_Y}
     * 响应参数类型：{@link Map}
     */
    public static final String KEY_R = "r";
    /**
     * 请求参数
     * 请求参数类型：{@link Map}
     */
    public static final String KEY_A = "a";
    /**
     * 错误编码
     * 错误编码类型：{@link Map}
     * 
     * @see ErrorCode
     */
    public static final String KEY_E = "e";
    /**
     * 客户端版本
     */
    public static final String KEY_V = "v";
    /**
     * NodeId
     * 
     * @see NodeContext#nodeId()
     */
    public static final String KEY_ID = "id";
    /**
     * 下载端口
     * 
     * @see QType#ANNOUNCE_PEER
     * @see SystemConfig#getTorrentPortExt()
     */
    public static final String KEY_PORT = "port";
    /**
     * Token
     * 
     * @see QType#ANNOUNCE_PEER
     */
    public static final String KEY_TOKEN = "token";
    /**
     * IPv4节点列表
     * 
     * @see QType#FIND_NODE
     * @see QType#GET_PEERS
     */
    public static final String KEY_NODES = "nodes";
    /**
     * IPv6节点列表
     * 
     * @see QType#FIND_NODE
     * @see QType#GET_PEERS
     */
    public static final String KEY_NODES6 = "nodes6";
    /**
     * IPv4节点
     * 
     * @see #KEY_WANT
     */
    public static final String KEY_WANT_N4 = "n4";
    /**
     * IPv6节点
     * 
     * @see #KEY_WANT
     */
    public static final String KEY_WANT_N6 = "n6";
    /**
     * 请求返回节点类型
     * 
     * @see #KEY_WANT_N4
     * @see #KEY_WANT_N6
     * @see QType#FIND_NODE
     * @see QType#GET_PEERS
     */
    public static final String KEY_WANT = "want";
    /**
     * Peer列表
     * 
     * @see QType#GET_PEERS
     */
    public static final String KEY_VALUES = "values";
    /**
     * 目标（NodeId、InfoHash）
     * 
     * @see QType#FIND_NODE
     */
    public static final String KEY_TARGET = "target";
    /**
     * InfoHash
     * 
     * @see QType#GET_PEERS
     * @see QType#ANNOUNCE_PEER
     */
    public static final String KEY_INFO_HASH = "info_hash";
    /**
     * 是否自动获取Peer端口
     * 
     * @see #IMPLIED_PORT_AUTO
     * @see #IMPLIED_PORT_CONFIG
     * @see QType#ANNOUNCE_PEER
     */
    public static final String KEY_IMPLIED_PORT = "implied_port";
    /**
     * 自动配置：忽略端口配置
     * 使用UDP连接端口作为Peer端口（支持UTP）
     */
    public static final Integer IMPLIED_PORT_AUTO = 1;
    /**
     * 端口配置
     * 使用消息端口配置
     * 
     * @see #KEY_PORT
     */
    public static final Integer IMPLIED_PORT_CONFIG = 0;
    /**
     * Peer列表长度
     * 
     * @see QType#GET_PEERS
     */
    public static final int GET_PEER_SIZE = 32;
    /**
     * NodeId长度
     */
    public static final int NODE_ID_LENGTH = 20;
    /**
     * Node最大保存数量
     */
    public static final int MAX_NODE_SIZE = 1024;
    /**
     * DHT超时请求清理执行周期（分钟）
     */
    public static final int DHT_REQUEST_TIMEOUT_INTERVAL = 8;
    
    /**
     * 默认DHT节点
     * NodeID=host:port
     */
    private final Map<String, String> nodes = new LinkedHashMap<>();
    
    private static final DhtConfig INSTANCE = new DhtConfig();
    
    public static final DhtConfig getInstance() {
        return INSTANCE;
    }
    
    private DhtConfig() {
        super(DHT_CONFIG);
        this.init();
        this.release();
    }
    
    @Override
    public void init() {
        this.properties.entrySet().forEach(entry -> {
            final String nodeId  = (String) entry.getKey();
            final String address = (String) entry.getValue();
            if(StringUtils.isNotEmpty(nodeId) && StringUtils.isNotEmpty(address)) {
                this.nodes.put(nodeId, address);
            } else {
                LOGGER.warn("默认DHT节点注册失败：{} - {}", nodeId, address);
            }
        });
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("加载DHT节点数量：{}", this.nodes.size());
        }
    }

    @Override
    public void persistent() {
        final Map<String, String> data = NodeContext.getInstance().resize().stream()
            .filter(NodeSession::useable)
            .collect(Collectors.toMap(
                node -> StringUtils.hex(node.getId()),
                node -> SymbolConfig.Symbol.COLON.join(node.getHost(), node.getPort())
            ));
        this.persistent(data, DHT_CONFIG);
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("保存DHT节点数量：{}", data.size());
        }
    }
    
    /**
     * @return 默认DHT节点
     */
    public Map<String, String> getNodes() {
        return this.nodes;
    }
    
}
