package com.acgist.snail.downloader.ftp;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.DownloadException;
import com.acgist.snail.protocol.ProtocolContext;
import com.acgist.snail.protocol.ftp.FtpProtocol;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.Performance;

class FtpDownloaderTest extends Performance {
    
    @Test
    void testFtpDownloaderBuild() throws DownloadException {
        final String url = "ftp://localhost/ftp/FTPserver.exe";
        ProtocolContext.getInstance().register(FtpProtocol.getInstance()).available(true);
        final var taskSession = FtpProtocol.getInstance().buildTaskSession(url);
        final var downloader = taskSession.buildDownloader();
       // 不下载
//      downloader.run();
        assertNotNull(downloader);
        taskSession.delete();
    }

    @Test
    void testFtpDownloader() throws DownloadException {
        final String url = "ftp://localhost/ftp/中文文件.exe";
//      final String url = "ftp://localhost/ftp/FTPserver.exe";
        ProtocolContext.getInstance().register(FtpProtocol.getInstance()).available(true);
        final var taskSession = FtpProtocol.getInstance().buildTaskSession(url);
        final var downloader = taskSession.buildDownloader();
        downloader.run();
        final var file = new File(taskSession.getFile());
        assertTrue(file.exists());
        FileUtils.delete(taskSession.getFile());
        taskSession.delete();
    }
    
}
