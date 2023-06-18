package com.acgist.snail.downloader;

import com.acgist.snail.context.ITaskSession;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.DownloadException;
import com.acgist.snail.net.NetException;
import com.acgist.snail.utils.FileUtils;

/**
 * 多文件任务下载器
 * 
 * @author acgist
 */
public abstract class MultifileDownloader extends Downloader {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MultifileDownloader.class);

    /**
     * 下载锁
     * 下载时阻塞下载任务线程
     */
    protected final Object downloadLock = new Object();
    
    /**
     * @param taskSession 任务信息
     */
    protected MultifileDownloader(ITaskSession taskSession) {
        super(taskSession);
    }
    
    @Override
    public void open() throws NetException, DownloadException {
        // 新建文件目录：防止删除目录导致任务下载失败
        FileUtils.buildFolder(this.taskSession.getFile());
        // 加载下载
        this.loadDownload();
    }

    @Override
    public void download() throws DownloadException {
        if(this.downloadable()) {
            synchronized (this.downloadLock) {
                while(this.downloadable()) {
                    try {
                        // 修改等待时间防止过长时间下载（失败时间等待）：验证下载数据是否变化判断任务是否失败
                        this.downloadLock.wait(Long.MAX_VALUE);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        LOGGER.debug("线程等待异常", e);
                    }
                    this.completed = this.checkCompleted();
                }
            }
        }
    }
    
    @Override
    public void unlockDownload() {
        super.unlockDownload();
        synchronized (this.downloadLock) {
            this.downloadLock.notifyAll();
        }
    }
    
    /**
     * 加载下载
     * 加载下载所需资源同时开始下载
     * 
     * @throws DownloadException 下载异常
     */
    protected abstract void loadDownload() throws DownloadException;

    /**
     * 判断是否下载完成
     * 
     * @return 是否下载完成
     */
    protected abstract boolean checkCompleted();
    
}
