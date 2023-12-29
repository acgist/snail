package com.acgist.snail.net.torrent.tracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.config.TrackerConfig;
import com.acgist.snail.context.IContext;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.NetException;
import com.acgist.snail.net.torrent.TorrentSession;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.UrlUtils;

/**
 * Tracker上下文
 * 
 * @author acgist
 */
public final class TrackerContext implements IContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrackerContext.class);
    
    private static final TrackerContext INSTANCE = new TrackerContext();
    
    public static final TrackerContext getInstance() {
        return INSTANCE;
    }

    /**
     * TrackerSession Map
     * {@link TrackerSession#id()}=TrackerSession
     */
    private final Map<Integer, TrackerSession> trackerSessions;
    /**
     * TrackerLauncher Map
     * {@link TrackerLauncher#id()}=TrackerLauncher
     */
    private final Map<Integer, TrackerLauncher> trackerLaunchers;
    
    private TrackerContext() {
        this.trackerSessions = new ConcurrentHashMap<>();
        this.trackerLaunchers = new ConcurrentHashMap<>();
        this.register();
    }

    /**
     * 新建TrackerLauncher
     * 
     * @param trackerSession TrackerSession
     * @param torrentSession BT任务信息
     * 
     * @return TrackerLauncher
     */
    public TrackerLauncher buildTrackerLauncher(TrackerSession trackerSession, TorrentSession torrentSession) {
        final TrackerLauncher launcher = TrackerLauncher.newInstance(trackerSession, torrentSession);
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("加载TrackerLauncher：{}-{}-{}", launcher.id(), trackerSession.id(), trackerSession.announceUrl());
        }
        this.trackerLaunchers.put(launcher.id(), launcher);
        return launcher;
    }
    
    /**
     * 删除TrackerLauncher
     * 
     * @param id {@link TrackerLauncher#id()}
     */
    public void removeTrackerLauncher(Integer id) {
        LOGGER.debug("删除TrackerLauncher：{}", id);
        this.trackerLaunchers.remove(id);
    }
    
    /**
     * 处理声明信息
     * 
     * @param message 消息
     */
    public void announce(AnnounceMessage message) {
        if(message == null) {
            return;
        }
        final Integer id = message.id();
        final TrackerLauncher trackerLauncher = this.trackerLaunchers.get(id);
        if(trackerLauncher != null) {
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("""
                    收到声明响应消息：{}
                    做种Peer数量：{}
                    下载Peer数量：{}
                    下次请求等待时间：{}""",
                    trackerLauncher.announceUrl(),
                    message.seeder(),
                    message.leecher(),
                    message.interval()
                );
            }
            trackerLauncher.announce(message);
        } else {
            LOGGER.debug("TrackerLauncher没有注册（声明消息）：{}", message);
        }
    }
    
    /**
     * 处理刮擦消息
     * 
     * @param message 消息
     */
    public void scrape(ScrapeMessage message) {
        if(message == null) {
            return;
        }
        final Integer id = message.id();
        final TrackerLauncher trackerLauncher = this.trackerLaunchers.get(id);
        if(trackerLauncher != null) {
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("""
                    收到刮擦响应消息：{}
                    做种Peer数量：{}
                    下载Peer数量：{}
                    完成Peer数量：{}""",
                    trackerLauncher.announceUrl(),
                    message.seeder(),
                    message.leecher(),
                    message.completed()
                );
            }
        } else {
            LOGGER.debug("TrackerLauncher没有注册（刮擦消息）：{}", message);
        }
    }
    
    /**
     * 处理连接ID消息
     * 
     * @param trackerId    {@link TrackerSession#id()}
     * @param connectionId 连接ID
     */
    public void connectionId(int trackerId, long connectionId) {
        final var trackerSession = this.trackerSessions.get(trackerId);
        if(trackerSession instanceof UdpTrackerSession udpTrackerSession) {
            // 类型判断不用判断是否为空
            udpTrackerSession.connectionId(connectionId);
        }
    }
    
    /**
     * @return TrackerSession拷贝
     */
    public List<TrackerSession> sessions() {
        return new ArrayList<>(this.trackerSessions.values());
    }

    /**
     * @param announceUrl 声明地址
     * 
     * @return TrackerSession列表
     * 
     * @see #sessions(String, List)
     */
    public List<TrackerSession> sessions(String announceUrl) {
        return this.sessions(announceUrl, null);
    }
    
    /**
     * @param announceUrls 声明地址集合
     * 
     * @return TrackerSession列表
     * 
     * @see #sessions(String, List)
     */
    public List<TrackerSession> sessions(List<String> announceUrls) {
        return this.sessions(null, announceUrls);
    }
    
    /**
     * @param announceUrl  声明地址
     * @param announceUrls 声明地址集合
     * 
     * @return TrackerSession列表
     * 
     * @see #sessions(String, List, boolean)
     */
    public List<TrackerSession> sessions(String announceUrl, List<String> announceUrls) {
        return this.sessions(announceUrl, announceUrls, false);
    }

    /**
     * @param announceUrl    声明地址
     * @param announceUrls   声明地址集合
     * @param privateTorrent 是否是私有种子
     * 
     * @return TrackerSession列表
     * 
     * @see #sessions(int, List)
     */
    public List<TrackerSession> sessions(String announceUrl, List<String> announceUrls, boolean privateTorrent) {
        final List<TrackerSession> sessions = this.buildTrackerSession(announceUrl, announceUrls);
        if(privateTorrent) {
            LOGGER.debug("私有种子：禁止补充Tracker");
            return sessions;
        }
        final int size = sessions.size();
        final int maxSize = SystemConfig.getTrackerSize();
        if(size < maxSize) {
            final var subjoin = this.sessions(maxSize - size, sessions);
            if(CollectionUtils.isNotEmpty(subjoin)) {
                sessions.addAll(subjoin);
            }
        }
        return sessions;
    }
    
    /**
     * 补充TrackerSession
     * 
     * @param size     补充数量
     * @param sessions 现有客户端
     * 
     * @return TrackerSession列表
     */
    private List<TrackerSession> sessions(int size, List<TrackerSession> sessions) {
        return this.trackerSessions.values().stream()
            .filter(client -> client.available() && !sessions.contains(client))
            // 排序：权重
            .sorted()
            .limit(size)
            .collect(Collectors.toList());
    }
    
    /**
     * 注册默认Tracker服务器
     */
    private void register() {
        this.buildTrackerSession(TrackerConfig.getInstance().getAnnounces());
    }
    
    /**
     * 新建TrackerSession列表
     * 
     * @param announceUrl  声明地址
     * @param announceUrls 声明地址集合
     * 
     * @return TrackerSession列表
     * 
     * @see #buildTrackerSession(List)
     */
    private List<TrackerSession> buildTrackerSession(String announceUrl, List<String> announceUrls) {
        final List<String> announces = new ArrayList<>();
        if(StringUtils.isNotEmpty(announceUrl)) {
            announces.add(announceUrl);
        }
        if(CollectionUtils.isNotEmpty(announceUrls)) {
            announces.addAll(announceUrls);
        }
        return this.buildTrackerSession(announces);
    }

    /**
     * 新建TrackerSession列表
     * 
     * @param announceUrls 声明地址集合
     * 
     * @return TrackerSession列表
     * 
     * @see #buildTrackerSession(String)
     */
    private List<TrackerSession> buildTrackerSession(List<String> announceUrls) {
        if(announceUrls == null) {
            announceUrls = new ArrayList<>();
        }
        return announceUrls.stream()
            .map(announceUrl -> this.buildTrackerSession(announceUrl.strip()))
            .filter(Objects::nonNull)
            .filter(TrackerSession::available)
            .collect(Collectors.toList());
    }
    
    /**
     * 新建TrackerSession
     * 
     * @param announceUrl 声明地址
     * 
     * @return TrackerSession
     * 
     * @see #buildTrackerSessionProxy(String)
     */
    private TrackerSession buildTrackerSession(String announceUrl) {
        if(StringUtils.isEmpty(announceUrl)) {
            return null;
        }
        final Optional<TrackerSession> optional = this.trackerSessions.values().stream()
            .filter(client -> client.equalsAnnounceUrl(announceUrl))
            .findFirst();
        if(optional.isPresent()) {
            return optional.get();
        }
        try {
            final TrackerSession session = this.buildTrackerSessionProxy(announceUrl);
            this.trackerSessions.put(session.id(), session);
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("注册TrackerSession：{}-{}", session.id(), session.announceUrl());
            }
            return session;
        } catch (NetException e) {
            LOGGER.error("注册TrackerSession异常：{}", announceUrl, e);
        } catch (Exception e) {
            LOGGER.error("注册TrackerSession异常：{}", announceUrl, e);
        }
        return null;
    }

    /**
     * 新建TrackerSession代理
     * 
     * @param announceUrl 声明地址
     * 
     * @return TrackerSession
     * 
     * @throws NetException 网络异常
     * 
     * @see #buildTrackerSessionProtocol(String)
     */
    private TrackerSession buildTrackerSessionProxy(final String announceUrl) throws NetException {
        TrackerSession session = this.buildTrackerSessionProtocol(announceUrl);
        if(session == null) {
            // 注册失败URL解码重试
            session = this.buildTrackerSessionProtocol(UrlUtils.decode(announceUrl));
        }
        if(session == null) {
            throw new NetException("未知Tracker协议：" + announceUrl);
        }
        return session;
    }

    /**
     * 新建TrackerSession协议
     * 
     * @param announceUrl 声明地址
     * 
     * @return TrackerSession
     * 
     * @throws NetException 网络异常
     */
    private TrackerSession buildTrackerSessionProtocol(final String announceUrl) throws NetException {
        if(Protocol.Type.HTTP.verify(announceUrl)) {
            return HttpTrackerSession.newInstance(announceUrl);
        } else if(Protocol.Type.UDP.verify(announceUrl)) {
            return UdpTrackerSession.newInstance(announceUrl);
        }
        return null;
    }
    
}
