package com.acgist.snail.protocol.thunder;

import java.util.Base64;

import com.acgist.snail.context.ITaskSession;
import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.DownloadException;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.protocol.ProtocolContext;
import com.acgist.snail.utils.StringUtils;

/**
 * 迅雷协议
 * 
 * @author acgist
 */
public final class ThunderProtocol extends Protocol {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ThunderProtocol.class);
    
    private static final ThunderProtocol INSTANCE = new ThunderProtocol();
    
    public static final ThunderProtocol getInstance() {
        return INSTANCE;
    }
    
    /**
     * 迅雷链接拼接前缀：{@value}
     */
    private static final String THUNDER_PREFIX = "AA";
    /**
     * 迅雷链接拼接后缀：{@value}
     */
    private static final String THUNDER_SUFFIX = "ZZ";

    private ThunderProtocol() {
        super(Type.THUNDER, "迅雷链接");
    }
    
    @Override
    public boolean available() {
        return true;
    }
    
    @Override
    public IDownloader buildDownloader(ITaskSession taskSession) {
        return null;
    }
    
    @Override
    public synchronized ITaskSession buildTaskSession(final String url) throws DownloadException {
        final String sourceUrl = this.sourceUrl(url);
        LOGGER.debug("迅雷原始链接：{}", sourceUrl);
        final var sourceProtocol = ProtocolContext.getInstance().protocol(sourceUrl);
        if(sourceProtocol.isEmpty()) {
            throw new DownloadException("不支持的下载链接：" + url);
        }
        return sourceProtocol.get().buildTaskSession(sourceUrl);
    }
    
    /**
     * 迅雷链接转换原始链接
     * 
     * @param url 迅雷链接
     * 
     * @return 原始链接
     */
    public String sourceUrl(String url) {
        final String prefix = Protocol.Type.THUNDER.prefix(url);
        String sourceUrl = url.substring(prefix.length());
        sourceUrl = new String(Base64.getMimeDecoder().decode(sourceUrl));
        sourceUrl = sourceUrl.substring(THUNDER_PREFIX.length(), sourceUrl.length() - THUNDER_SUFFIX.length());
        return sourceUrl;
    }
    
    /**
     * 原始链接转换迅雷链接
     * 
     * @param url 原始链接
     * 
     * @return 迅雷链接
     */
    public String thunderUrl(String url) {
        String thunderUrl = THUNDER_PREFIX + url + THUNDER_SUFFIX;
        thunderUrl = Base64.getMimeEncoder().encodeToString(thunderUrl.getBytes());
        thunderUrl = Protocol.Type.THUNDER.defaultPrefix() + thunderUrl;
        return StringUtils.replaceAllBlank(thunderUrl);
    }
    
}
