package com.acgist.snail.downloader.magnet;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.DownloadException;
import com.acgist.snail.net.torrent.TorrentInitializer;
import com.acgist.snail.protocol.ProtocolContext;
import com.acgist.snail.protocol.magnet.MagnetProtocol;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.Performance;

class MagnetDownloaderTest extends Performance {

    @Test
    void testMagnetDownloaderBuild() throws DownloadException {
//      final String url = "902FFAA29EE632C8DC966ED9AB573409BA9A518E";
        final String url = "magnet:?xt=urn:btih:902FFAA29EE632C8DC966ED9AB573409BA9A518E";
        ProtocolContext.getInstance().register(MagnetProtocol.getInstance()).available(true);
        final var taskSession = MagnetProtocol.getInstance().buildTaskSession(url);
        final var downloader = taskSession.buildDownloader();
        // 不下载
//      downloader.run();
        assertNotNull(downloader);
        final var file = new File(taskSession.getFile());
        assertTrue(file.exists());
        FileUtils.delete(taskSession.getFile());
        taskSession.delete();
    }

    @Test
    void testMagnetDownloader() throws DownloadException {
        TorrentInitializer.newInstance().sync();
        // 推荐使用活跃磁力链接测试
//      final String url = "902FFAA29EE632C8DC966ED9AB573409BA9A518E";
        final String url = "magnet:?xt=urn:btih:902FFAA29EE632C8DC966ED9AB573409BA9A518E";
        ProtocolContext.getInstance().register(MagnetProtocol.getInstance()).available(true);
        final var taskSession = MagnetProtocol.getInstance().buildTaskSession(url);
        final var downloader = taskSession.buildDownloader();
        downloader.run();
        final var file = new File(taskSession.getFile());
        assertTrue(file.exists());
        assertTrue(ArrayUtils.isNotEmpty(file.list()));
        FileUtils.delete(taskSession.getFile());
        taskSession.delete();
    }
    
}
