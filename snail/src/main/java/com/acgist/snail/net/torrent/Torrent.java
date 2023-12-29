package com.acgist.snail.net.torrent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.acgist.snail.format.BEncodeDecoder;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * 种子信息
 * 
 * @author acgist
 */
public final class Torrent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 注释：{@value}
     */
    public static final String ATTR_COMMENT = "comment";
    /**
     * 注释（UTF8）：{@value}
     */
    public static final String ATTR_COMMENT_UTF8 = "comment.utf-8";
    /**
     * 编码：{@value}
     */
    public static final String ATTR_ENCODING = "encoding";
    /**
     * 创建者：{@value}
     */
    public static final String ATTR_CREATED_BY = "created by";
    /**
     * 创建时间：{@value}
     */
    public static final String ATTR_CREATION_DATE = "creation date";
    /**
     * Tracker服务器：{@value}
     */
    public static final String ATTR_ANNOUNCE = "announce";
    /**
     * Tracker服务器列表：{@value}
     */
    public static final String ATTR_ANNOUNCE_LIST = "announce-list";
    /**
     * 文件信息：{@value}
     */
    public static final String ATTR_INFO = "info";
    /**
     * DHT节点：{@value}
     */
    public static final String ATTR_NODES = "nodes";
    /**
     * DHT节点列表长度：{@value}
     */
    private static final int NODE_LIST_LENGTH = 2;
    
    /**
     * 注释
     */
    private String comment;
    /**
     * 注释（UTF8）
     */
    private String commentUtf8;
    /**
     * 编码
     */
    private String encoding;
    /**
     * 创建者
     */
    private String createdBy;
    /**
     * 创建时间
     */
    private Long creationDate;
    /**
     * Tracker服务器
     */
    private String announce;
    /**
     * Tracker服务器列表
     */
    private List<String> announceList;
    /**
     * 文件信息
     */
    private TorrentInfo info;
    /**
     * DHT节点列表
     */
    private Map<String, Integer> nodes;
    /**
     * InfoHash
     * 种子文件加载完成保存InfoHash：防止重复计算导致错误
     */
    private transient InfoHash infoHash;

    protected Torrent() {
    }
    
    /**
     * 读取种子信息
     * 
     * @param decoder 种子信息
     * 
     * @return 种子信息
     */
    public static final Torrent valueOf(BEncodeDecoder decoder) {
        Objects.requireNonNull(decoder, "种子信息为空");
        final Torrent torrent = new Torrent();
        final String encoding = decoder.getString(ATTR_ENCODING);
        torrent.setEncoding(encoding);
        torrent.setComment(decoder.getString(ATTR_COMMENT, encoding));
        torrent.setCommentUtf8(decoder.getString(ATTR_COMMENT_UTF8));
        torrent.setCreatedBy(decoder.getString(ATTR_CREATED_BY));
        torrent.setCreationDate(decoder.getLong(ATTR_CREATION_DATE));
        torrent.setAnnounce(decoder.getString(ATTR_ANNOUNCE));
        torrent.setAnnounceList(readAnnounceList(decoder.getList(ATTR_ANNOUNCE_LIST)));
        torrent.setInfo(TorrentInfo.valueOf(decoder.getMap(ATTR_INFO), encoding));
        torrent.setNodes(readNodes(decoder.getList(ATTR_NODES)));
        return torrent;
    }
    
    /**
     * @return 任务名称
     */
    public String name() {
        String name = this.info.getNameUtf8();
        if(StringUtils.isEmpty(name)) {
            name = this.info.getName();
        }
        return name;
    }
    
    /**
     * @return InfoHash
     */
    public InfoHash infoHash() {
        return this.infoHash;
    }

    /**
     * @param infoHash InfoHash
     */
    public void infoHash(InfoHash infoHash) {
        this.infoHash = infoHash;
    }
    
    /**
     * 读取Tracker服务器列表
     * 
     * @param announceList Tracker服务器数据
     * 
     * @return Tracker服务器列表
     */
    private static final List<String> readAnnounceList(List<Object> announceList) {
        if(announceList == null) {
            return new ArrayList<>(0);
        }
        return announceList.stream()
            .flatMap(value -> ((List<?>) value).stream())
            .map(StringUtils::getString)
            .collect(Collectors.toList());
    }
    
    /**
     * 读取DHT节点列表
     * 
     * @param nodes DHT节点列表数据
     * 
     * @return DHT节点列表
     */
    private static final Map<String, Integer> readNodes(List<Object> nodes) {
        if(nodes == null) {
            return new LinkedHashMap<>();
        }
        return nodes.stream()
            .map(Torrent::readNode)
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new));
    }

    /**
     * 读取节点信息
     * 
     * @param value 节点信息
     * 
     * @return 节点
     */
    private static Map.Entry<String, Integer> readNode(Object value) {
        final List<?> values = (List<?>) value;
        if(values.size() == NODE_LIST_LENGTH) {
            String host = StringUtils.getString(values.get(0));
            if(StringUtils.isNumeric(host)) {
                // 紧凑型地址
                host = NetUtils.intToIP(Integer.parseInt(host));
            }
            final Long port = (Long) values.get(1);
            return Map.entry(host, port.intValue());
        } else {
            return null;
        }
    }
    
    /**
     * @return 注释
     */
    public String getComment() {
        return this.comment;
    }

    /**
     * @param comment 注释
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * @return 注释（UTF8）
     */
    public String getCommentUtf8() {
        return this.commentUtf8;
    }

    /**
     * @param commentUtf8 注释（UTF8）
     */
    public void setCommentUtf8(String commentUtf8) {
        this.commentUtf8 = commentUtf8;
    }

    /**
     * @return 编码
     */
    public String getEncoding() {
        return this.encoding;
    }

    /**
     * @param encoding 编码
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * @return 创建者
     */
    public String getCreatedBy() {
        return this.createdBy;
    }

    /**
     * @param createdBy 创建者
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * @return 创建时间
     */
    public Long getCreationDate() {
        return this.creationDate;
    }

    /**
     * @param creationDate 创建时间
     */
    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * @return Tracker服务器
     */
    public String getAnnounce() {
        return this.announce;
    }

    /**
     * @param announce Tracker服务器
     */
    public void setAnnounce(String announce) {
        this.announce = announce;
    }

    /**
     * @return Tracker服务器列表
     */
    public List<String> getAnnounceList() {
        return this.announceList;
    }

    /**
     * @param announceList Tracker服务器列表
     */
    public void setAnnounceList(List<String> announceList) {
        this.announceList = announceList;
    }

    /**
     * @return 文件信息
     */
    public TorrentInfo getInfo() {
        return this.info;
    }

    /**
     * @param info 文件信息
     */
    public void setInfo(TorrentInfo info) {
        this.info = info;
    }

    /**
     * @return DHT节点列表
     */
    public Map<String, Integer> getNodes() {
        return this.nodes;
    }

    /**
     * @param nodes DHT节点列表
     */
    public void setNodes(Map<String, Integer> nodes) {
        this.nodes = nodes;
    }
    
    @Override
    public String toString() {
        // TODO：实现
        return super.toString();
    }

}
