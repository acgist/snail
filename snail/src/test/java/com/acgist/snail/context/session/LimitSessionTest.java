package com.acgist.snail.context.session;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.DownloadConfig;
import com.acgist.snail.context.session.LimitSession.Type;
import com.acgist.snail.utils.Performance;

class LimitSessionTest extends Performance {

    @Test
    void testLimitSession() throws InterruptedException {
        final LimitSession session = new LimitSession(Type.UPLOAD);
        final long size = DownloadConfig.getUploadBufferByte();
        this.log("上传限速大小：{}", size);
        final int buffer = 1024;
        this.cost();
        final var a = new Thread(() -> {
            int value = 0;
            while(value < size) {
                value += buffer;
                session.limit(buffer);
            }
        });
        final var b = new Thread(() -> {
            int value = 0;
            while(value < size) {
                value += buffer;
                session.limit(buffer);
            }
        });
        a.start();
        b.start();
        a.join();
        b.join();
        assertTrue(this.costed() >= 1000);
    }
    
}
