package com.acgist.snail.downloader.http;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.DownloadException;
import com.acgist.snail.protocol.ProtocolContext;
import com.acgist.snail.protocol.http.HttpProtocol;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.Performance;

class HttpDownloaderTest extends Performance {
    
    @Test
    void testHttpDownloaderBuild() throws DownloadException {
        final String url = "https://mirrors.bfsu.edu.cn/apache/tomcat/tomcat-9/v9.0.41/bin/apache-tomcat-9.0.41.zip";
        ProtocolContext.getInstance().register(HttpProtocol.getInstance()).available(true);
        final var taskSession = HttpProtocol.getInstance().buildTaskSession(url);
        final var downloader = taskSession.buildDownloader();
       // 不下载
//      downloader.run();
        assertNotNull(downloader);
//      taskSession.delete();
    }

    @Test
    void testHttpDownloader() throws DownloadException {
        final String url = "https://mirrors.bfsu.edu.cn/apache/tomcat/tomcat-9/v9.0.41/bin/apache-tomcat-9.0.41.zip";
        ProtocolContext.getInstance().register(HttpProtocol.getInstance()).available(true);
        final var taskSession = HttpProtocol.getInstance().buildTaskSession(url);
        final var downloader = taskSession.buildDownloader();
        downloader.run();
        final var file = new File(taskSession.getFile());
        assertTrue(file.exists());
        FileUtils.delete(taskSession.getFile());
        taskSession.delete();
    }
    
}
