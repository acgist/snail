package com.acgist.snail.net.torrent.tracker;

import java.util.Objects;

import com.acgist.snail.config.TrackerConfig;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.NetException;
import com.acgist.snail.net.torrent.TorrentSession;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.utils.BeanUtils;
import com.acgist.snail.utils.NumberUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * Tracker信息
 * 基本协议：HTTP、UDP
 * 
 * @author acgist
 */
public abstract class TrackerSession implements Comparable<TrackerSession> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TrackerSession.class);
    
    /**
     * 想要获取Peer数量：{@value}
     */
    protected static final int WANT_PEER_SIZE = 50;
    
    /**
     * 客户端ID
     * 对应Tracker服务器
     */
    protected final Integer id;
    /**
     * 协议类型
     */
    protected final Protocol.Type type;
    /**
     * 刮擦地址
     */
    protected final String scrapeUrl;
    /**
     * 声明地址
     */
    protected final String announceUrl;
    /**
     * 权重
     * 查询成功会使权重增加
     * 查询失败会使权重减少
     */
    protected int weight;
    /**
     * 失败次数
     * 查询成功会使失败次数增加
     * 查询失败会使失败次数减少
     * 
     * @see TrackerConfig#MAX_FAIL_TIMES
     */
    protected int failTimes = 0;
    /**
     * 是否可用
     */
    protected boolean available = true;
    
    /**
     * Tracker信息
     * 
     * @param scrapeUrl   刮擦地址
     * @param announceUrl 声明地址
     * @param type        协议类型
     * 
     * @throws NetException 网络异常
     */
    protected TrackerSession(String scrapeUrl, String announceUrl, Protocol.Type type) throws NetException {
        if(StringUtils.isEmpty(announceUrl)) {
            throw new NetException("Tracker声明地址错误：" + announceUrl);
        }
        this.id = NumberUtils.build();
        this.type = type;
        this.weight = 0;
        this.scrapeUrl = scrapeUrl;
        this.announceUrl = announceUrl;
    }

    /**
     * 查找Peer
     * 查找到的结果放入Peer列表
     * 
     * @param sid            {@link TrackerLauncher#id()}
     * @param torrentSession BT任务信息
     */
    public void findPeers(Integer sid, TorrentSession torrentSession) {
        if(!this.available()) {
            return;
        }
        try {
            // 发送声明消息
            this.started(sid, torrentSession);
            this.weight++;
            this.failTimes = 0;
        } catch (Exception e) {
            this.weight--;
            this.failTimes++;
            if(this.failTimes >= TrackerConfig.MAX_FAIL_TIMES) {
                this.available = false;
                LOGGER.error("Tracker停用，失败次数：{}，声明地址：{}", this.failTimes, this.announceUrl, e);
            } else {
                LOGGER.debug("查找Peer异常，失败次数：{}，声明地址：{}", this.failTimes, this.announceUrl, e);
            }
        }
    }
    
    /**
     * 声明：开始
     * 
     * @param sid            {@link TrackerLauncher#id()}
     * @param torrentSession BT任务信息
     * 
     * @throws NetException 网络异常
     */
    public abstract void started(Integer sid, TorrentSession torrentSession) throws NetException;
    
    /**
     * 声明：完成
     * 任务完成时发送
     * 
     * @param sid            {@link TrackerLauncher#id()}
     * @param torrentSession BT任务信息
     * 
     * @throws NetException 网络异常
     */
    public abstract void completed(Integer sid, TorrentSession torrentSession) throws NetException;
    
    /**
     * 声明：停止
     * 任务暂停时发送
     * 
     * @param sid            {@link TrackerLauncher#id()}
     * @param torrentSession BT任务信息
     * 
     * @throws NetException 网络异常
     */
    public abstract void stopped(Integer sid, TorrentSession torrentSession) throws NetException;
    
    /**
     * 刮檫
     * 
     * @param sid            {@link TrackerLauncher#id()}
     * @param torrentSession BT任务信息
     * 
     * @throws NetException 网络异常
     */
    public abstract void scrape(Integer sid, TorrentSession torrentSession) throws NetException;
    
    /**
     * 新建声明消息
     * 
     * @param sid            {@link TrackerLauncher#id()}
     * @param torrentSession BT任务信息
     * @param event 事件
     * 
     * @return 声明消息
     */
    protected Object buildAnnounceMessage(Integer sid, TorrentSession torrentSession, TrackerConfig.Event event) {
        // 剩余下载大小
        long left = 0L;
        // 已经上传大小
        long upload = 0L;
        // 已经下载大小
        long download = 0L;
        final var taskSession = torrentSession.taskSession();
        if(taskSession != null) {
            final var statistics = taskSession.getStatistics();
            upload = statistics.getUploadSize();
            download = statistics.getDownloadSize();
            left = taskSession.getSize() - download;
        }
        return this.buildAnnounceMessageEx(sid, torrentSession, event, upload, download, left);
    }
    
    /**
     * 新建声明消息
     * 
     * @param sid            {@link TrackerLauncher#id()}
     * @param torrentSession BT任务信息
     * @param event          事件
     * @param upload         已经上传大小
     * @param download       已经下载大小
     * @param left           剩余下载大小
     * 
     * @return 声明消息
     */
    protected abstract Object buildAnnounceMessageEx(Integer sid, TorrentSession torrentSession, TrackerConfig.Event event, long upload, long download, long left);
    
    /**
     * @return 客户端ID
     */
    public Integer id() {
        return this.id;
    }
    
    /**
     * @return 协议类型
     */
    public Protocol.Type type() {
        return this.type;
    }
    
    /**
     * @return 刮擦地址
     */
    public String scrapeUrl() {
        return this.scrapeUrl;
    }
    
    /**
     * @return 声明地址
     */
    public String announceUrl() {
        return this.announceUrl;
    }
    
    /**
     * 判断是否可用
     * 
     * @return 是否可用
     */
    public boolean available() {
        return this.available;
    }
    
    /**
     * 判断当前Tracker声明地址和声明地址是否相同
     * 
     * @param announceUrl 声明地址
     * 
     * @return 是否相同
     */
    public boolean equalsAnnounceUrl(String announceUrl) {
        return this.announceUrl.equals(announceUrl);
    }
    
    @Override
    public int compareTo(TrackerSession session) {
        return Integer.compare(this.weight, session.weight);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(this.announceUrl);
    }
    
    @Override
    public boolean equals(Object object) {
        if(this == object) {
            return true;
        }
        if(object instanceof TrackerSession session) {
            return StringUtils.equals(this.announceUrl, session.announceUrl);
        }
        return false;
    }
    
    @Override
    public String toString() {
        return BeanUtils.toString(this, this.id, this.type, this.scrapeUrl, this.announceUrl);
    }
    
}
