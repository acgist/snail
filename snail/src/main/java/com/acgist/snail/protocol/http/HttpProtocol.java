package com.acgist.snail.protocol.http;

import com.acgist.snail.context.ITaskSession;
import com.acgist.snail.context.wrapper.HttpHeaderWrapper;
import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.downloader.http.HttpDownloader;
import com.acgist.snail.net.DownloadException;
import com.acgist.snail.net.NetException;
import com.acgist.snail.net.http.HttpClient;
import com.acgist.snail.protocol.Protocol;

/**
 * HTTP协议
 * 
 * @author acgist
 */
public final class HttpProtocol extends Protocol {

    private static final HttpProtocol INSTANCE = new HttpProtocol();
    
    public static final HttpProtocol getInstance() {
        return INSTANCE;
    }

    /**
     * HTTP头部信息
     */
    private HttpHeaderWrapper httpHeaderWrapper;
    
    private HttpProtocol() {
        super(Type.HTTP, "HTTP");
    }
    
    @Override
    public boolean available() {
        return true;
    }
    
    @Override
    public IDownloader buildDownloader(ITaskSession taskSession) {
        return HttpDownloader.newInstance(taskSession);
    }

    @Override
    protected void prep() throws DownloadException {
        this.buildHttpHeader();
    }
    
    @Override
    protected String buildFileName() throws DownloadException {
        // 优先使用头部信息中的文件名称
        final String defaultName = super.buildFileName();
        return this.httpHeaderWrapper.getFileName(defaultName);
    }

    @Override
    protected void buildSize() {
        this.taskEntity.setSize(this.httpHeaderWrapper.fileSize());
    }
    
    @Override
    protected void release(boolean success) {
        this.httpHeaderWrapper = null;
        super.release(success);
    }

    /**
     * 新建HTTP头部信息
     * 
     * @throws DownloadException 下载异常
     */
    private void buildHttpHeader() throws DownloadException {
        try {
            this.httpHeaderWrapper = HttpClient
                .newInstance(this.url)
                .head()
                .responseHeader();
        } catch (NetException e) {
            throw new DownloadException("获取HTTP头部信息失败", e);
        }
    }
    
}
