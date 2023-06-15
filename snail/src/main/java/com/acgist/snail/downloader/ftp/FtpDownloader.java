package com.acgist.snail.downloader.ftp;

import java.nio.channels.Channels;

import com.acgist.snail.context.ITaskSession;
import com.acgist.snail.downloader.MonofileDownloader;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.NetException;
import com.acgist.snail.net.ftp.FtpClient;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.IoUtils;

/**
 * FTP任务下载器
 * 
 * @author acgist
 */
public final class FtpDownloader extends MonofileDownloader {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(FtpDownloader.class);
    
    /**
     * FTP客户端
     */
    private FtpClient client;
    
    /**
     * @param taskSession 任务信息
     */
    private FtpDownloader(ITaskSession taskSession) {
        super(taskSession);
    }

    /**
     * @param taskSession 任务信息
     * 
     * @return {@link FtpDownloader}
     */
    public static final FtpDownloader newInstance(ITaskSession taskSession) {
        return new FtpDownloader(taskSession);
    }

    @Override
    public void release() {
        if(this.client != null) {
            this.client.close();
        }
        IoUtils.close(this.input);
        IoUtils.close(this.output);
        super.release();
    }
    
    @Override
    protected void buildInput() throws NetException {
        // 连接
        this.client = FtpClient.newInstance(this.taskSession.getUrl());
        if(this.client.connect()) {
            // 打开流
            final long downloadSize = FileUtils.fileSize(this.taskSession.getFile());
            this.input = Channels.newChannel(this.client.download(downloadSize));
            // 断点续传
            if(this.client.range()) {
                LOGGER.debug("FTP断点下载：{}", downloadSize);
                this.taskSession.setDownloadSize(downloadSize);
            } else {
                LOGGER.debug("FTP重新下载：{}", downloadSize);
                this.taskSession.setDownloadSize(0L);
            }
        } else {
            this.fail("FTP服务器连接失败");
        }
    }
    
}
