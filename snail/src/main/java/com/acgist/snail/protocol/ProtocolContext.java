package com.acgist.snail.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import com.acgist.snail.Snail;
import com.acgist.snail.context.IContext;
import com.acgist.snail.context.ITaskSession;
import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.DownloadException;
import com.acgist.snail.utils.StringUtils;

/**
 * 下载协议上下文
 * 
 * @author acgist
 */
public final class ProtocolContext implements IContext {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolContext.class);

    private static final ProtocolContext INSTANCE = new ProtocolContext();
    
    public static final ProtocolContext getInstance() {
        return INSTANCE;
    }
    
    /**
     * 下载协议
     */
    private final List<Protocol> protocols;
    /**
     * 可用状态锁
     * 协议没有加载完成阻塞所有获取协议线程
     */
    private final AtomicBoolean availableLock;
    
    private ProtocolContext() {
        this.protocols = new ArrayList<>();
        this.availableLock = new AtomicBoolean(false);
    }
    
    /**
     * 注册协议
     * 
     * @param protocol 协议
     * 
     * @return ProtocolContext
     */
    public ProtocolContext register(Protocol protocol) {
        if(this.protocols.contains(protocol)) {
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("下载协议已经注册：{}", protocol.name());
            }
        } else {
            if(LOGGER.isInfoEnabled()) {
                LOGGER.info("注册下载协议：{}", protocol.name());
            }
            this.protocols.add(protocol);
        }
        return this;
    }
    
    /**
     * 新建下载器
     * 
     * @param taskSession 任务信息
     * 
     * @return 下载器
     * 
     * @throws DownloadException 下载异常
     */
    public IDownloader buildDownloader(ITaskSession taskSession) throws DownloadException {
        final Protocol.Type type = taskSession.getType();
        final Optional<Protocol> optional = this.protocols.stream()
            .filter(Protocol::available)
            .filter(protocol -> protocol.type() == type)
            .findFirst();
        if(optional.isEmpty()) {
            throw new DownloadException("不支持的下载类型：" + type);
        }
        final IDownloader downloader = optional.get().buildDownloader(taskSession);
        if(downloader == null) {
            throw new DownloadException("不支持的下载类型：" + type);
        }
        return downloader;
    }
    
    /**
     * 新建下载任务
     * 
     * @param url 下载链接
     * 
     * @return 任务信息
     * 
     * @throws DownloadException 下载异常
     */
    public ITaskSession buildTaskSession(String url) throws DownloadException {
        final Optional<Protocol> protocol = this.protocol(url);
        if(protocol.isEmpty()) {
            throw new DownloadException("不支持的下载链接：" + url);
        }
        return protocol.get().buildTaskSession(url);
    }

    /**
     * 判断是否支持下载
     * 
     * @param url 下载链接
     * 
     * @return 是否支持
     */
    public boolean support(String url) {
        return this.protocol(url).isPresent();
    }

    /**
     * @param url 下载链接
     * 
     * @return 下载协议
     */
    public Optional<Protocol> protocol(final String url) {
        if(StringUtils.isEmpty(url)) {
            return Optional.empty();
        }
        final String verify = url.strip();
        return this.protocols.stream()
            .filter(Protocol::available)
            .filter(protocol -> protocol.verify(verify))
            .findFirst();
    }

    /**
     * 判断状态是否可用（阻塞线程）
     * 
     * @return 是否可用
     * 
     * @throws DownloadException 下载异常
     */
    public boolean available() throws DownloadException {
        if(!this.availableLock.get()) {
            synchronized (this.availableLock) {
                if(!this.availableLock.get()) {
                    try {
                        this.availableLock.wait(Short.MAX_VALUE);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        LOGGER.debug("线程等待异常", e);
                    }
                }
            }
        }
        if(Snail.available()) {
            return true;
        } else {
            throw new DownloadException("系统没有启动");
        }
    }

    /**
     * 设置可用状态（释放阻塞线程）
     * 
     * @param available 是否可用
     */
    public void available(boolean available) {
        synchronized (this.availableLock) {
            this.availableLock.set(available);
            this.availableLock.notifyAll();
        }
    }
    
}
