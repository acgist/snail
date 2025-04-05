package com.acgist.snail.protocol.hls;

import java.nio.file.Files;
import java.nio.file.Paths;

import com.acgist.snail.config.SymbolConfig.Symbol;
import com.acgist.snail.context.ITaskSession;
import com.acgist.snail.context.wrapper.DescriptionWrapper;
import com.acgist.snail.context.wrapper.URIWrapper;
import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.downloader.hls.HlsDownloader;
import com.acgist.snail.net.DownloadException;
import com.acgist.snail.net.NetException;
import com.acgist.snail.net.hls.HlsContext;
import com.acgist.snail.net.hls.M3u8;
import com.acgist.snail.net.http.HttpClient;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * HLS协议
 * 协议链接：https://tools.ietf.org/html/rfc8216
 * 
 * @author acgist
 */
public final class HlsProtocol extends Protocol {

    private static final HlsProtocol INSTANCE = new HlsProtocol();
    
    /**
     * 默认结尾：{@value}
     */
    private static final String INDEX_M3U8 = "/index.m3u8";
    
    public static final HlsProtocol getInstance() {
        return INSTANCE;
    }
    
    /**
     * M3U8信息
     */
    private M3u8 m3u8;
    
    private HlsProtocol() {
        super(Type.HLS, "HLS");
    }

    @Override
    public boolean available() {
        return true;
    }

    @Override
    public IDownloader buildDownloader(ITaskSession taskSession) {
        return HlsDownloader.newInstance(taskSession);
    }
    
    @Override
    protected void prep() throws DownloadException {
        try {
            this.buildM3u8();
        } catch (NetException e) {
            throw new DownloadException("网络异常", e);
        }
    }
    
    @Override
    protected String buildFileName() throws DownloadException {
        if(Protocol.Type.HTTP.verify(this.url)) {
            final String path = URIWrapper.newInstance(this.url).decode().getPath();
            if(StringUtils.endsWithIgnoreCase(path, INDEX_M3U8)) {
                // 去掉斜杠和结尾
                return path.substring(1, path.length() - INDEX_M3U8.length())
                    .replace(Symbol.SLASH.toChar(), Symbol.MINUS.toChar()) +
                    Protocol.Type.HLS.defaultSuffix();
            }
        }
        return super.buildFileName();
    }
    
    @Override
    protected void buildSize() throws DownloadException {
        this.taskEntity.setSize(0L);
    }
    
    @Override
    protected void done() throws DownloadException {
        this.buildFolder();
        this.selectFiles();
    }

    /**
     * 新建M3U8信息
     * 
     * @throws NetException      网络异常
     * @throws DownloadException 下载异常
     */
    private void buildM3u8() throws NetException, DownloadException {
        final String response;
        if(Protocol.Type.HTTP.verify(this.url)) {
            response = HttpClient
               .newInstance(this.url)
               .get()
               .responseToString();
        } else {
            try {
                response = Files.readString(Paths.get(this.url));
            } catch (Exception e) {
                throw new DownloadException("无效文件：" + this.url, e);
            }
        }
        final var m3u8Check = M3u8Builder.newInstance(response, this.url).build();
        if(m3u8Check.getType() == M3u8.Type.M3U8) {
            this.url = m3u8Check.getMaxRateLink();
            this.buildM3u8();
        } else if(m3u8Check.getType() == M3u8.Type.STREAM) {
            throw new DownloadException("不支持直播流媒体下载");
        } else {
            this.m3u8 = m3u8Check;
        }
    }
    
    /**
     * 新建下载目录
     */
    private void buildFolder() {
        FileUtils.buildFolder(this.taskEntity.getFile());
    }
    
    /**
     * 保持下载文件列表
     */
    private void selectFiles() {
        // M3U8协议默认下载所有文件
        this.taskEntity.setDescription(DescriptionWrapper.newEncoder(this.m3u8.getLinks()).serialize());
    }
    
    @Override
    protected void success() {
        // 成功添加管理
        HlsContext.getInstance().m3u8(this.taskEntity.getId(), this.m3u8);
    }
    
    @Override
    protected void release(boolean success) {
        this.m3u8 = null;
        super.release(success);
    }

}
