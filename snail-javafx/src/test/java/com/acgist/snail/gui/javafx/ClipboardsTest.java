package com.acgist.snail.gui.javafx;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

import javafx.application.Platform;

class ClipboardsTest extends Performance {

    @Test
    void testClipboards() throws InterruptedException {
        Platform.startup(() -> {});
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                final String content = "测试";
                Clipboards.copy(content);
                assertEquals(content, Clipboards.get());
            } catch (Exception e) {
                this.log("testClipboards", e);
            } finally {
                latch.countDown();
            }
        });
        latch.await();
    }
    
}
