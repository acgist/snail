package com.acgist.snail.gui.event;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.acgist.snail.gui.GuiContext;
import com.acgist.snail.gui.event.adapter.BuildEventAdapter;
import com.acgist.snail.logger.Level;
import com.acgist.snail.logger.LoggerConfig;
import com.acgist.snail.utils.Performance;

class BuildEventAdapterTest extends Performance {

    @Test
    void testEvent() {
        LoggerConfig.setLevel(Level.INFO);
        final AtomicInteger index = new AtomicInteger();
        final BuildEventAdapter adapter = new BuildEventAdapter() {
            @Override
            protected void executeExtend(Object ... args) {
                index.incrementAndGet();
                BuildEventAdapterTest.this.log("扩展");
            }
            @Override
            protected void executeNative(Object ... args) {
                index.incrementAndGet();
                BuildEventAdapterTest.this.log("本地");
            }
        };
        GuiContext.register(adapter);
        GuiContext.getInstance().build();
        GuiContext.getInstance().init("mode=extend");
        GuiContext.register(adapter);
        GuiContext.getInstance().build();
        assertEquals(2, index.get());
    }
    
}
