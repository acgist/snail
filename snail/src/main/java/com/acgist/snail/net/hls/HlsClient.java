package com.acgist.snail.net.hls;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import com.acgist.snail.config.DownloadConfig;
import com.acgist.snail.context.ITaskSession;
import com.acgist.snail.context.wrapper.HttpHeaderWrapper;
import com.acgist.snail.downloader.Downloader;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.NetException;
import com.acgist.snail.net.http.HttpClient;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.IoUtils;

/**
 * HLS客户端
 * 
 * @author acgist
 */
public final class HlsClient implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(HlsClient.class);
    
    /**
     * 下载路径
     */
    private final String link;
    /**
     * 下载文件路径
     */
    private final String path;
    /**
     * 文件大小
     */
    private long size;
    /**
     * 是否支持断点续传
     */
    private boolean range;
    /**
     * 是否下载完成
     */
    private volatile boolean completed;
    /**
     * HLS任务信息
     */
    private final HlsSession hlsSession;
    /**
     * 输入流
     */
    private ReadableByteChannel input;
    /**
     * 输出流
     */
    private WritableByteChannel output;
    /**
     * 共享本地缓存
     */
    private final ThreadLocal<ByteBuffer> threadLocal;
    
    /**
     * @param link        下载路径
     * @param taskSession 任务信息
     * @param hlsSession  HLS任务信息
     * @param threadLocal 本地缓存
     */
    public HlsClient(String link, ITaskSession taskSession, HlsSession hlsSession, ThreadLocal<ByteBuffer> threadLocal) {
        this.link        = link;
        this.path        = FileUtils.file(taskSession.getFile(), FileUtils.fileName(link));
        this.range       = false;
        this.completed   = false;
        this.hlsSession  = hlsSession;
        this.threadLocal = threadLocal;
    }
    
    @Override
    public void run() {
        if(!this.downloadable()) {
            LOGGER.debug("HLS任务不能下载：{}", this.link);
            return;
        }
        LOGGER.debug("HLS任务下载文件：{}", this.link);
        // 已经下载大小
        long downloadSize = FileUtils.fileSize(this.path);
        this.completed = this.checkCompleted(downloadSize);
        if(this.completed) {
            LOGGER.debug("HLS文件校验成功：{}", this.link);
        } else {
            int length = 0;
            // 共享ByteBuffer
            final ByteBuffer buffer = this.threadLocal.get();
            buffer.clear();
//          final ByteBuffer buffer = ByteBuffer.allocateDirect(SystemConfig.DEFAULT_EXCHANGE_LENGTH);
            try {
                this.buildInput(downloadSize);
                this.buildOutput();
                // 不支持断点续传：重置已经下载大小
                if(!this.range) {
                    downloadSize = 0L;
                }
                while(this.downloadable()) {
                    length = this.input.read(buffer);
                    if(length >= 0) {
                        buffer.flip();
                        this.output.write(buffer);
                        buffer.compact();
                        downloadSize += length;
                        this.hlsSession.download(length);
                    }
                    if(Downloader.checkFinish(length, downloadSize, this.size)) {
                        this.completed = true;
                        break;
                    }
                }
            } catch (Exception e) {
                LOGGER.error("HLS任务下载异常：{}", this.link, e);
            }
        }
        this.release();
        if(this.completed) {
            LOGGER.debug("HLS文件下载完成：{}", this.link);
            this.hlsSession.remove(this);
            this.hlsSession.downloadSize(downloadSize);
            this.hlsSession.checkCompletedAndUnlock();
        } else {
            LOGGER.debug("HLS文件下载失败（重新下载）：{}", this.link);
            // 下载失败重新添加下载
            this.hlsSession.download(this);
        }
    }

    /**
     * @return 是否可以下载
     */
    private boolean downloadable() {
        return !this.completed && this.hlsSession.downloadable();
    }
    
    /**
     * @param downloadSize 已经下载大小
     * 
     * @return 是否下载完成
     */
    private boolean checkCompleted(final long downloadSize) {
        // 如果文件已经下载完成直接返回
        if(this.completed) {
            return this.completed;
        }
        final File file = new File(this.path);
        if(!file.exists()) {
            return false;
        }
        try {
            final HttpHeaderWrapper header = HttpClient
                .newInstance(this.link)
                .head()
                .responseHeader();
            this.size = header.fileSize();
            return this.size == downloadSize;
        } catch (NetException e) {
            LOGGER.error("HLS文件校验异常：{}", this.link, e);
        }
        return false;
    }

    /**
     * 新建{@linkplain #input 输入流}
     * 
     * @param downloadSize 已经下载大小
     * 
     * @throws NetException 网络异常
     */
    private void buildInput(final long downloadSize) throws NetException {
        final HttpClient client = HttpClient
            .newDownloader(this.link)
            .range(downloadSize)
            .get();
        if(client.downloadable()) {
            final HttpHeaderWrapper headers = client.responseHeader();
            this.range = headers.range();
            this.input = Channels.newChannel(client.response());
        } else {
            throw new NetException("HLS客户端输入流新建失败");
        }
    }
    
    /**
     * 新建{@linkplain #output 输出流}
     * 
     * @throws NetException 网络异常
     */
    private void buildOutput() throws NetException {
        try {
            final int bufferSize = DownloadConfig.getMemoryBufferByte(this.size);
            OutputStream outputStream;
            if(this.range) {
                // 支持断点续传
                outputStream = new FileOutputStream(this.path, true);
            } else {
                // 不支持断点续传
                outputStream = new FileOutputStream(this.path);
            }
            final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream, bufferSize);
            this.output = Channels.newChannel(bufferedOutputStream);
        } catch (FileNotFoundException e) {
            throw new NetException("HLS客户端输出流新建失败", e);
        }
    }

    /**
     * 释放资源
     * 由于Client下载完成没有立即从队列中删除，所以导致内存一直增长，需要手动设置输入流和输出流为空方便内存回收。
     */
    public void release() {
        LOGGER.debug("HLS客户端释放：{}", this.link);
        IoUtils.close(this.input);
        this.input = null;
        IoUtils.close(this.output);
        this.output = null;
    }
    
}
