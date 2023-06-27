package com.acgist.snail.gui.event;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.acgist.snail.gui.GuiContext;
import com.acgist.snail.gui.GuiContext.MessageType;
import com.acgist.snail.gui.event.adapter.AlertEventAdapter;
import com.acgist.snail.logger.Level;
import com.acgist.snail.logger.LoggerConfig;
import com.acgist.snail.utils.Performance;

class AlertEventAdapterTest extends Performance {

    @Test
    void testEvent() {
        LoggerConfig.setLevel(Level.INFO);
        final AtomicInteger index = new AtomicInteger();
        final AlertEventAdapter adapter = new AlertEventAdapter() {
            @Override
            protected void executeExtendExtend(MessageType type, String title, String message) {
                index.incrementAndGet();
                AlertEventAdapterTest.this.log("扩展");
            }
            @Override
            protected void executeNativeExtend(MessageType type, String title, String message) {
                index.incrementAndGet();
                AlertEventAdapterTest.this.log("本地");
            }
        };
        GuiContext.register(adapter);
        GuiContext.getInstance().alert("acgist", "snail");
        GuiContext.getInstance().init("mode=extend");
        GuiContext.register(adapter);
        GuiContext.getInstance().alert("acgist", "snail");
        assertEquals(2, index.get());
    }
    
}
