package com.acgist.snail.downloader.http;

import java.nio.channels.Channels;

import com.acgist.snail.context.ITaskSession;
import com.acgist.snail.context.wrapper.HttpHeaderWrapper;
import com.acgist.snail.downloader.MonofileDownloader;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.NetException;
import com.acgist.snail.net.http.HttpClient;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.IoUtils;

/**
 * HTTP任务下载器
 * 
 * @author acgist
 */
public final class HttpDownloader extends MonofileDownloader {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpDownloader.class);

    /**
     * @param taskSession 任务信息
     */
    private HttpDownloader(ITaskSession taskSession) {
        super(taskSession);
    }

    /**
     * @param taskSession 任务信息
     * 
     * @return {@link HttpDownloader}
     */
    public static final HttpDownloader newInstance(ITaskSession taskSession) {
        return new HttpDownloader(taskSession);
    }
    
    @Override
    public void release() {
        IoUtils.close(this.input);
        IoUtils.close(this.output);
        super.release();
    }
    
    @Override
    protected void buildInput() throws NetException {
        // 连接
        final long downloadSize = FileUtils.fileSize(this.taskSession.getFile());
        final HttpClient client = HttpClient
            .newDownloader(this.taskSession.getUrl())
            .range(downloadSize)
            .get();
        if(client.downloadable()) {
            // 打开流
            final HttpHeaderWrapper headers = client.responseHeader();
            this.input = Channels.newChannel(client.response());
            // 断点续传
            if(headers.range()) {
                LOGGER.debug("HTTP断点下载：{}", downloadSize);
                this.taskSession.setDownloadSize(downloadSize);
            } else {
                LOGGER.debug("HTTP重新下载：{}", downloadSize);
                this.taskSession.setDownloadSize(0L);
            }
        } else if(this.taskSession.getDownloadSize() == this.taskSession.getSize()) {
            // 优先验证下载文件大小：416=超出请求范围
            this.completed = true;
        } else {
            this.fail("HTTP请求失败：" + client.getCode());
        }
    }

}
